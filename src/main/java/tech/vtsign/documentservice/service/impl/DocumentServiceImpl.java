package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.exception.LockedException;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.exception.SignedException;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.DocumentRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentProducer;
import tech.vtsign.documentservice.service.DocumentService;

import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final AzureStorageService azureStorageService;
    private final UserServiceProxy userServiceProxy;
    private final ContractRepository contractRepository;
    private final DocumentProducer documentProducer;
    private final DocumentRepository documentRepository;
    private final ContractService contractService;
    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;


    @Value("${tech.vtsign.hostname}")
    private String hostname = "http://localhost/";
    @Value("${server.servlet.context-path}")
    private String contextPath = "/document";
    @Value("${tech.vtsign.kafka.document-service.notify-sign}")
    private String TOPIC_SIGN;

    @Override
    public boolean createUserDocument(DocumentClientRequest clientRequest, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        boolean success = true;

        try {
            List<UserContract> userContracts = new ArrayList<>();
            List<Document> documents = new ArrayList<>();
            for (MultipartFile file : files) {
                String saveName = String.format("%s-%s", UUID.randomUUID(), file.getOriginalFilename());
                String urlDocument = this.uploadFile(saveName, file.getBytes());
                Document document = new Document(urlDocument);
                document.setOriginName(file.getOriginalFilename());
                document.setSaveName(saveName);
                documents.add(document);
            }


            User user = new User();
            user.setId(senderInfo.getId());
            user.setEmail(senderInfo.getEmail());
            User userSenderSaved = userRepository.save(user);

            Contract contract = new Contract();
            contract.setTitle(clientRequest.getMailTitle());
            contract.setSenderUUID(senderInfo.getId());
            contract.setSentDate(new Date());
            contract.setDocuments(documents);
            Contract contractSaved = contractRepository.save(contract);

            UserContract userContract = new UserContract();
//            userContract.setId(UUID.randomUUID());
            userContract.setStatus(DocumentStatus.WAITING);
            userContract.setViewedDate(new Date());
            userContract.setSignedDate(new Date());

            userContract.setUser(userSenderSaved);
            userContract.setContract(contractSaved);
            userContracts.add(userContract);

            for (Receiver receiver : clientRequest.getReceivers()) {
                UserContract userContractTemp = this.getUserDocument(receiver);
                userContractTemp.setContract(contractSaved);
                userContractTemp.setPermission(receiver.getPermission());
                userContractTemp.setSecretKey(receiver.getKey());
                userContracts.add(userContractTemp);
            }
            userDocumentRepository.saveAll(userContracts);
//
            this.sendEmailSign(contractSaved, clientRequest, senderInfo.getFullName());

        } catch (Exception ex) {
            success = false;
            ex.printStackTrace();
        }
        return success;

    }

    @Override
    public String uploadFile(String name, byte[] bytes) {
        return azureStorageService.uploadNotOverride(name, bytes);
    }

    private List<UserContract> generateListUserDocument(List<Receiver> receivers, Contract contract) {
        List<UserContract> listUserContract = new ArrayList<>();
        for (Receiver receiver : receivers) {
            UserContract userContract = getUserDocument(receiver);
            userContract.setContract(contract);
//            UserContract
            userDocumentRepository.save(userContract);
            listUserContract.add(userContract);
        }
        return listUserContract;
    }


    public void sendMail(Object object, String topic) {
        documentProducer.sendMessage(object, topic);
    }

    public void sendEmailSign(Contract contract, DocumentClientRequest clientRequest, String senderFullName) {
        clientRequest.getReceivers().forEach(receiver -> {
            String url = String.format("%s/signDocument?c=%s&r=%s",
                    hostname,
                    contract.getId(), receiver.getId()
            );
            InfoMailReceiver infoMailReceiver =
                    new InfoMailReceiver(receiver.getName(), receiver.getEmail(), receiver.getPrivateMessage(), clientRequest.getMailMessage(), clientRequest.getMailTitle(), url, senderFullName);

            this.sendMail(infoMailReceiver, TOPIC_SIGN);
        });


    }

    private UserContract getUserDocument(Receiver receiver) {
        LoginServerResponseDto userReceiver = userServiceProxy.getOrCreateUser(receiver.getEmail(), receiver.getName());
        receiver.setId(userReceiver.getId());
        User user = new User();
        user.setId(userReceiver.getId());
        user.setEmail(receiver.getEmail());
        User userSaved = userRepository.save(user);

        UserContract userContract = new UserContract();
        userContract.setStatus(DocumentStatus.ACTION_REQUIRE);
        userContract.setUser(userSaved);
        return userContract;
    }

    @Override
    public Document getById(UUID uuid) {
        return documentRepository.getById(uuid);
    }

    @Transactional
    @Override
    public UserContractResponse getUDRByContractIdAndUserId(UUID contractId, UUID userUUID, String secretKey) {
        LoginServerResponseDto user = userServiceProxy.getUserById(userUUID);
        UserContract userContract = contractService.findUserContractByIdAndUserId(contractId, userUUID);

        if (!userContract.getSecretKey().equals(secretKey)) {
            throw new LockedException("Secret Key does not match");
        }
        Contract contract = userContract.getContract();
        Optional<User> contractOwnerOpt = userRepository.findById(contract.getSenderUUID());
        User contractOwner = contractOwnerOpt.orElseThrow(() -> new NotFoundException("Not found user"));
        if (userContract.getStatus().equals(DocumentStatus.SIGNED))
            throw new SignedException("A Contract was signed by this User");
        UserContractResponse userContractResponse = new UserContractResponse();

        boolean lastSign = contract.getUserContracts().stream()
                .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                .count() == contract.getUserContracts().size() - 2;
        if (userContract.getViewedDate() == null) {
            userContract.setViewedDate(new Date());
            InfoMailReceiver info = new InfoMailReceiver();
            info.setEmail(contractOwner.getEmail());
            info.setMailTitle("Viewed");
            info.setMailMessage("");
            this.sendMail(info, TOPIC_SIGN);
        }
        userContractResponse.setUser(user);
        userContractResponse.setDocuments(contract.getDocuments());
        userContractResponse.setLastSign(lastSign);
        return userContractResponse;
    }

}
