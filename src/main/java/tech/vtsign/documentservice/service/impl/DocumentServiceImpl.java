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

import java.util.*;
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
        String regexPhone = "^(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        String regexEmail = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";


        for (Receiver receiver : clientRequest.getReceivers()) {
            if (receiver.getPhone() != null && !Pattern.matches(regexPhone, receiver.getPhone())
                    || receiver.getEmail() == null
                    || receiver.getEmail() != null && !Pattern.matches(regexEmail, receiver.getEmail())) {
                throw new InvalidFormatException("format of phone or mail does not correct ");
            }
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        boolean success = true;


        Set<UserContract> userContracts = new HashSet<>();
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
            if (receiver.getPhone() != null)
                receiver.setPhone(this.replacePhone(receiver.getPhone()));
            UserContract userContractTemp = this.getUserContract(receiver);
            userContractTemp.setContract(contractSaved);
            userContractTemp.setPermission(receiver.getPermission());
            userContractTemp.setSecretKey(receiver.getKey());
            userContractTemp.setPublicMessage(clientRequest.getMailMessage());
            userContracts.add(userContractTemp);
        }
        contractSaved.setUserContracts(userContracts);
        List<UserContract> userContractList = userDocumentRepository.saveAll(userContracts);

        this.sendEmailSign(contractSaved, clientRequest, senderInfo.getFullName(), userContractList);


        return success;

    }

    private String replacePhone(String phone) {
        String result = phone;
        if (phone.startsWith("0")) {
            result = phone.replaceFirst("0", "+84");
        }
        return result;
    }

    @Override
    public String uploadFile(String name, byte[] bytes) {
        return azureStorageService.uploadNotOverride(name, bytes);
    }

    public void sendMail(Object object, String topic) {
        documentProducer.sendMessage(object, topic);
    }

    public void sendEmailSign(Contract contract, DocumentClientRequest clientRequest, String senderName, List<UserContract> userContracts) {
        userContracts.forEach(uc -> {
            User user = uc.getUser();
            if (uc.getPermission() != null && uc.getPermission().equals("sign")) {
                String url = String.format("%s/signDocument?c=%s&r=%s&uc=%s",
                        hostname,
                        contract.getId(), user.getId(), uc.getId()
                );
                ReceiverContract receiverContract = new ReceiverContract();
                BeanUtils.copyProperties(clientRequest, receiverContract);
                Receiver receiver = new Receiver();
                receiver.setName(user.getFullName());
                receiver.setKey(uc.getSecretKey());
                BeanUtils.copyProperties(user, receiver);
                receiverContract.setReceiver(receiver);
                receiverContract.setUrl(url);
                receiverContract.setSenderName(senderName);
                receiverContract.setCreatedDate(contract.getSentDate());
                this.sendMail(receiverContract, TOPIC_SIGN);
            }
        });

    }

    private UserContract getUserContract(Receiver receiver) {

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
        if (receiver.getPermission().equals("read"))
            userContract.setStatus(DocumentStatus.READ);
        else if (receiver.getPermission().equals("sign"))
            userContract.setStatus(DocumentStatus.ACTION_REQUIRE);
        userContract.setUser(userSaved);
        return userContract;
    }

    @Override
    public Document getById(UUID uuid) {
        return documentRepository.getById(uuid);
    }
}
