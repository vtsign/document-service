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

            Contract contract = new Contract();
//            contract.setSenderUUID(senderInfo.getId());
            contract.setSentDate(new Date());
            contract.setDocuments(documents);

            UserContract userContract = new UserContract();
            userContract.setId(UUID.randomUUID());
            userContract.setStatus(DocumentStatus.WAITING);
            userContract.setViewedDate(new Date());
            userContract.setSignedDate(new Date());

            userContract.setUser(user);
            userContract.setContract(contract);



            UserContract userContractSaved =  userDocumentRepository.save(userContract);
            for (Receiver receiver : clientRequest.getReceivers()) {
                UserContract userContractTemp = this.getUserDocument(receiver);
                userContractTemp.setContract(contract);
////            UserContract
                userDocumentRepository.save(userContractTemp);
            }

//            List<UserContract> listUserContract = generateListUserDocument(clientRequest.getReceivers(),contract);
//            listUserContract.add(userContract);



//            Contract savedContract = contractRepository.save(contract);
//            // sent mail
            this.sendEmail(userContractSaved.getContract(), clientRequest, senderInfo.getFullName());

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


    public void sendEmail(Contract contract, DocumentClientRequest clientRequest, String senderFullName) {
        clientRequest.getReceivers().forEach(receiver -> {
            String url = String.format("%s/signDocument?c=%s&r=%s",
                    hostname,
                    contract.getId(), receiver.getId()
            );
            InfoMailReceiver infoMailReceiver =
                    new InfoMailReceiver(receiver.getName(), receiver.getEmail(), receiver.getPrivateMessage(), clientRequest.getMailMessage(), clientRequest.getMailTitle(), url, senderFullName);

            documentProducer.sendMessage(infoMailReceiver, TOPIC_SIGN);
        });


    }

    private UserContract getUserDocument(Receiver receiver) {
        LoginServerResponseDto userReceiver = userServiceProxy.getOrCreateUser(receiver.getEmail(), receiver.getName());
        receiver.setId(userReceiver.getId());
        User user = new User();
        user.setId(userReceiver.getId());
        user.setEmail(receiver.getEmail());
        UserContract userContract = new UserContract();
        userContract.setId(UUID.randomUUID());
        userContract.setStatus(DocumentStatus.ACTION_REQUIRE);
        userContract.setUser(user);
        return userContract;
    }

    @Override
    public Document getById(UUID uuid) {
        return documentRepository.getById(uuid);
    }

    @Override
    public UserContractResponse getUDRByContractIdAndUserId(UUID contractId, UUID userUUID) {
        LoginServerResponseDto user = userServiceProxy.getUserById(userUUID);
        List<Document> documents = contractService.getDocumentsByContractAndReceiver(contractId, userUUID);
//        UserContract userContract = userDocumentRepository.findByUserUUIDAndContractId(userUUID,contractId);
        Optional<User> opt = userRepository.findById(userUUID);
        UserContract userContract = new UserContract();
        if (opt.isPresent()) {
            User userL = opt.get();
            Set<UserContract> userContracts = userL.getUserContracts();
            Optional<UserContract> optContract = userContracts.stream().filter(u -> u.getContract().getId().equals(contractId)).findFirst();
            userContract = optContract.orElseThrow(() -> new NotFoundException("Not found contract"));

        }
        if (userContract.getStatus().equals(DocumentStatus.SIGNED))
            throw new SignedException("A Contract was signed by this User");

        UserContractResponse userContractResponse = new UserContractResponse();

        Contract contract = contractService.getContractById(contractId);
        boolean lastSign = contract.getUserContracts().stream()
                .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                .count() == contract.getUserContracts().size() - 2;
        InfoMailReceiver info = new InfoMailReceiver();
        info.setName(user.getFullName());
//        info.setEmail();
//        documentProducer.sendMessage();
        userContractResponse.setUser(user);
        userContractResponse.setDocuments(documents);
        userContractResponse.setLastSign(lastSign);
        return userContractResponse;
    }
}
