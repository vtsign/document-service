package tech.vtsign.documentservice.service.impl;

import com.google.common.primitives.Bytes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.DigitalSignature;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.DocumentProducer;
import tech.vtsign.documentservice.service.DocumentService;
import tech.vtsign.documentservice.utils.DSUtil;
import tech.vtsign.documentservice.utils.FileUtil;
import tech.vtsign.documentservice.utils.KeyReaderUtil;

import java.security.PrivateKey;
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

    @Value("${tech.vtsign.hostname}")
    private String hostname = "http://localhost/";
    @Value("${server.servlet.context-path}")
    private String contextPath = "/document";

    @Override
    public boolean createDigitalSignature(DocumentClientRequest clientRequest, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        boolean success = true;

        try {
            List<Document> documents = new ArrayList<>();
            byte[] totalBytes = new byte[0];
            for (MultipartFile file : files) {
                byte[] fileBytes = file.getBytes();
                String urlDocument = this.uploadFile(
                        String.format("%s-%s", UUID.randomUUID(), file.getOriginalFilename()),
                        fileBytes);
                Document document = new Document(urlDocument);
                documents.add(document);
                totalBytes = Bytes.concat(totalBytes, fileBytes);
            }

            byte[] digitalSignatureSender = this.createSignatureByte(totalBytes, senderInfo.getPrivateKey());
            String urlSignature = this.uploadFile(
                    String.format("%s/%s", senderInfo.getId(), UUID.randomUUID()), digitalSignatureSender);


            DigitalSignature senderDigital = DigitalSignature.builder()
                    .url(urlSignature)
                    .userUUID(senderInfo.getId())
                    .status(DocumentStatus.WAITING)
                    .publicKey(senderInfo.getPublicKey())
                    .build();

            List<DigitalSignature> listDigitalSignature = generateDigitalSignatureReceiver(clientRequest.getReceivers());
            listDigitalSignature.add(senderDigital);

            Contract contract = Contract.builder()
                    .senderUUID(senderInfo.getId())
                    .sentDate(new Date())
                    .documents(documents)
                    .digitalSignatures(listDigitalSignature)
                    .build();

            Contract savedContract = contractRepository.save(contract);
            // sent mail
            this.sendEmail(savedContract, clientRequest.getReceivers(), senderInfo.getFullName());

        } catch (Exception ex) {
            success = false;
            ex.printStackTrace();
        }
        return success;

    }

    @Override
    public byte[] createSignature(MultipartFile file, String privateKeyUrl) throws Exception {
        byte[] privateKeyBytes = FileUtil.readByteFromURL(privateKeyUrl);
        PrivateKey privateKey = KeyReaderUtil.getPrivateKey(privateKeyBytes);
        return DSUtil.sign(file.getBytes(), privateKey);
    }

    @SneakyThrows
    private byte[] createSignatureByte(byte[] file, String privateKeyUrl) throws Exception {
        byte[] privateKeyBytes = FileUtil.readByteFromURL(privateKeyUrl);
        PrivateKey privateKey = KeyReaderUtil.getPrivateKey(privateKeyBytes);
        return DSUtil.sign(file, privateKey);
    }

    @Override
    public String uploadFile(String name, byte[] bytes) {
        return azureStorageService.uploadNotOverride(name, bytes);
    }

    private List<DigitalSignature> generateDigitalSignatureReceiver(List<Receiver> receivers) {
        //Tao
        List<DigitalSignature> listDigitalSignature = new ArrayList<>();
        for (Receiver receiver : receivers) {
            DigitalSignature userDS = getDigitalSignature(receiver);
            listDigitalSignature.add(userDS);
        }
        return listDigitalSignature;
    }


    public void sendEmail(Contract contract, List<Receiver> receivers, String fullName) {
        receivers.forEach(receiver -> {
            String url = String.format("%s%s/signing/?c=%s&r=%s",
                    hostname, contextPath,
                    contract.getId(), receiver.getId()
            );
            InfoMailReceiver infoMailReceiver = new InfoMailReceiver(receiver.getEmail(), url, fullName);

            documentProducer.sendMessage(infoMailReceiver);
        });


    }

    private DigitalSignature getDigitalSignature(Receiver receiver) {
        LoginServerResponseDto user = userServiceProxy.getOrCreateUser(receiver.getEmail(), receiver.getName());
        receiver.setId(user.getId());
        return DigitalSignature.builder()
                .userUUID(user.getId())
                .status(DocumentStatus.ACTION_REQUIRE)
                .publicKey(user.getPublicKey())
                .build();
    }
}
