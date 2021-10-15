package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.DigitalSignature;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.model.DocumentStatus;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.model.Receiver;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.repository.DocumentRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
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
    private final DocumentRepository documentRepository;
    private final UserServiceProxy userServiceProxy;

    @Override
    public boolean createDigitalSignature(DocumentClientRequest clientRequest, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        boolean success = true;
        // foreach document
        // tạo chữ kí người gửi
        // push chữ kí người gửi và document lên storage
        // lưu database document và bảng digital_signalture người gửi (đã có url chữ kí) status chờ kí.
        // foreach người nhận
        // lay thong tin nguoi nhan (*)
        // tạo bảng digital_signature vs status cần kí lưu xuống database(khóa ngoại -> document)  chưa có url chữ kí
        // (*): user-service
        //nếu có tài khoản
        //nếu chưa có tài khoản -> tạo tài khoản vs status temp

        for (MultipartFile file : files) {
            try {
                byte[] senderSignature = this.createSignature(file, senderInfo.getPrivateKey());
                String urlDocument = this.uploadFile(
                        String.format("%s-%s", UUID.randomUUID(), file.getOriginalFilename()),
                        file.getBytes());
                String urlSignature = this.uploadFile(
                        String.format("%s/%s", senderInfo.getId(), UUID.randomUUID()),
                        senderSignature);

                List<DigitalSignature> digitalSignatures = new ArrayList<>();
                DigitalSignature senderDigital = DigitalSignature.builder()
                        .url(urlSignature)
                        .userUUID(senderInfo.getId())
                        .status(DocumentStatus.WAITING)
                        .build();

                digitalSignatures.add(senderDigital);

                Document document = this.generateDocument(clientRequest.getReceivers(), digitalSignatures, urlDocument);
                documentRepository.save(document);

            } catch (Exception e) {
                e.printStackTrace();
                success = false;
                break;
            }

        }

        return success;
    }

    @Override
    public byte[] createSignature(MultipartFile file, String privateKeyUrl) throws Exception {
        byte[] privateKeyBytes = FileUtil.readByteFromURL(privateKeyUrl);
        PrivateKey privateKey = KeyReaderUtil.getPrivateKey(privateKeyBytes);
        return DSUtil.sign(file.getBytes(), privateKey);
    }

    @Override
    public String uploadFile(String name, byte[] bytes) {
        return azureStorageService.uploadNotOverride(name, bytes);
    }

    private Document generateDocument(List<Receiver> receivers, List<DigitalSignature> digitalSignatures, String urlDocument) {
        for (Receiver receiver : receivers) {
            DigitalSignature userDS = getDigitalSignature(receiver);
            digitalSignatures.add(userDS);
        }
        return Document.builder()
                .url(urlDocument)
                .sentDate(new Date())
                .digitalSignatures(digitalSignatures)
                .build();
    }

    private DigitalSignature getDigitalSignature(Receiver receiver) {
        LoginServerResponseDto user = userServiceProxy.getOrCreateUser(receiver.getEmail(), receiver.getName());
        return DigitalSignature.builder()
                .userUUID(user.getId())
                .status(DocumentStatus.ACTION_REQUIRE)
                .build();
    }
}
