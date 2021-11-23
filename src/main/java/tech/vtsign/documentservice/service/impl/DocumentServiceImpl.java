package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.exception.InvalidFormatException;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.DocumentRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.DocumentProducer;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final AzureStorageService azureStorageService;
    private final UserServiceProxy userServiceProxy;
    private final ContractRepository contractRepository;
    private final DocumentProducer documentProducer;
    private final DocumentRepository documentRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;


    @Value("${tech.vtsign.hostname}")
    private String hostname = "http://localhost/";
    @Value("${server.servlet.context-path}")
    private String contextPath = "/document";
    @Value("${tech.vtsign.kafka.document-service.notify-sign}")
    private String TOPIC_SIGN;

    @SneakyThrows
    @Override
    public boolean createUserDocument(DocumentClientRequest clientRequest, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        boolean success = true;


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
            user.setPhone(senderInfo.getPhone());
            user.setFirstName(senderInfo.getFirstName());
            user.setLastName(senderInfo.getLastName());
            User userSenderSaved = userRepository.save(user);

            Contract contract = new Contract();
            contract.setTitle(clientRequest.getMailTitle());
            contract.setSentDate(new Date());
            contract.setLastModifiedDate(new Date());
            contract.setDocuments(documents);
            Contract contractSaved = contractRepository.save(contract);

            UserContract userContract = new UserContract();
            userContract.setStatus(DocumentStatus.WAITING);
            userContract.setOwner(true);
            userContract.setViewedDate(new Date());
            userContract.setSignedDate(new Date());

            userContract.setUser(userSenderSaved);
            userContract.setContract(contractSaved);
            userContracts.add(userContract);

            for (Receiver receiver : clientRequest.getReceivers()) {
                UserContract userContractTemp = this.getUserContract(receiver);
                userContractTemp.setContract(contractSaved);
                userContractTemp.setPermission(receiver.getPermission());
                userContractTemp.setSecretKey(receiver.getKey());
                userContractTemp.setPublicMessage(clientRequest.getMailMessage());
                userContracts.add(userContractTemp);
            }
            userDocumentRepository.saveAll(userContracts);

            this.sendEmailSign(contractSaved, clientRequest, senderInfo.getFullName());

//        } catch (Exception ex) {
//            success = false;
////            ex.printStackTrace();
//        }
        return success;

    }

    @Override
    public String uploadFile(String name, byte[] bytes) {
        return azureStorageService.uploadNotOverride(name, bytes);
    }

    public void sendMail(Object object, String topic) {
        documentProducer.sendMessage(object, topic);
    }

    public void sendEmailSign(Contract contract, DocumentClientRequest clientRequest, String senderName) {
        clientRequest.getReceivers().forEach(receiver -> {
            String url = String.format("%s/signDocument?c=%s&r=%s",
                    hostname,
                    contract.getId(), receiver.getId()
            );
            ReceiverContract receiverContract = new ReceiverContract();
            BeanUtils.copyProperties(clientRequest, receiverContract);
            receiverContract.setReceiver(receiver);
            receiverContract.setUrl(url);
            receiverContract.setSenderName(senderName);
            this.sendMail(receiverContract, TOPIC_SIGN);
        });


    }

    private UserContract getUserContract(Receiver receiver) {
        String regexPhone = "^(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        if (receiver.getPhone() != null && !Pattern.matches(regexPhone, receiver.getPhone())) {
            throw new InvalidFormatException("phone does not has this format");
        }
        LoginServerResponseDto userReceiver = userServiceProxy
                .getOrCreateUser(receiver.getEmail(), receiver.getPhone(), receiver.getName());
        receiver.setId(userReceiver.getId());

        User user = new User();
        String phone = receiver.getPhone() != null ? receiver.getPhone() : userReceiver.getPhone();
        user.setId(userReceiver.getId());
        user.setEmail(userReceiver.getEmail());
        user.setFirstName(userReceiver.getFirstName());
        user.setLastName(userReceiver.getLastName());
        receiver.setPhone(phone);
        user.setPhone(phone);
        User userSaved = userRepository.save(user);

        UserContract userContract = new UserContract();
        userContract.setPrivateMessage(receiver.getPrivateMessage());
        userContract.setStatus(DocumentStatus.ACTION_REQUIRE);
        userContract.setUser(userSaved);
        return userContract;
    }

    @Override
    public Document getById(UUID uuid) {
        return documentRepository.getById(uuid);
    }
}
