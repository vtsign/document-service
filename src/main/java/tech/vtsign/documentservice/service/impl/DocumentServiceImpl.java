package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.UserDocument;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.DocumentRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.DocumentProducer;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final AzureStorageService azureStorageService;
    private final UserServiceProxy userServiceProxy;
    private final ContractRepository contractRepository;
    private final DocumentProducer documentProducer;
    private final DocumentRepository documentRepository;

    @Value("${tech.vtsign.hostname}")
    private String hostname = "http://localhost/";
    @Value("${server.servlet.context-path}")
    private String contextPath = "/document";

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

            UserDocument userDocument = new UserDocument(DocumentStatus.WAITING, senderInfo.getId());
            userDocument.setViewedDate(new Date());
            userDocument.setSignedDate(new Date());

            List<UserDocument> listUserDocument = generateListUserDocument(clientRequest.getReceivers());
            listUserDocument.add(userDocument);

            Contract contract = new Contract();
            contract.setSenderUUID(senderInfo.getId());
            contract.setSentDate(new Date());
            contract.setDocuments(documents);
            contract.setUserDocuments(listUserDocument);

            Contract savedContract = contractRepository.save(contract);
            // sent mail
            this.sendEmail(savedContract, clientRequest, senderInfo.getFullName());

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

    private List<UserDocument> generateListUserDocument(List<Receiver> receivers) {
        List<UserDocument> listUserDocument = new ArrayList<>();
        for (Receiver receiver : receivers) {
            UserDocument userDocument = getUserDocument(receiver);
            listUserDocument.add(userDocument);
        }
        return listUserDocument;
    }


    public void sendEmail(Contract contract,DocumentClientRequest clientRequest , String senderFullName) {
        clientRequest.getReceivers().forEach(receiver -> {
            String url = String.format("%s%s/apt/signing/?c=%s&r=%s",
                    hostname, contextPath,
                    contract.getId(), receiver.getId()
            );
            InfoMailReceiver infoMailReceiver =
                    new InfoMailReceiver(receiver.getName(),receiver.getEmail(),receiver.getPrivateMessage(),clientRequest.getMailMessage(),clientRequest.getMailTitle(),url,senderFullName);

            documentProducer.sendMessage(infoMailReceiver);
        });


    }

    private UserDocument getUserDocument(Receiver receiver) {
        LoginServerResponseDto user = userServiceProxy.getOrCreateUser(receiver.getEmail(), receiver.getName());
        receiver.setId(user.getId());
        return new UserDocument(DocumentStatus.ACTION_REQUIRE, user.getId());
    }

    @Override
    public Document getById(UUID uuid) {
        return documentRepository.getById(uuid);
    }
}
