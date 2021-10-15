package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.DigitalSignature;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.model.Receiver;
import tech.vtsign.documentservice.model.User;
import tech.vtsign.documentservice.repository.DocumentRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.DocumentService;
import tech.vtsign.documentservice.utils.DSUtil;
import tech.vtsign.documentservice.utils.FileUtil;
import tech.vtsign.documentservice.utils.KeyReaderUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                byte[] privateKeyBytes = FileUtil.readByteFromURL(senderInfo.getPrivateKey());
                PrivateKey privateKey = KeyReaderUtil.getPrivateKey(privateKeyBytes);
                byte[] digitalSignature = DSUtil.sign(file.getBytes(), privateKey);
                String urlSignature = azureStorageService.uploadNotOverride(
                        String.format("%s/%s-%s", senderInfo.getId(), UUID.randomUUID(), file.getOriginalFilename()),
                        digitalSignature);
                String urlDocument = azureStorageService.uploadNotOverride(
                        String.format("%s-%s", UUID.randomUUID(), file.getOriginalFilename()),
                        file.getBytes());

                List<DigitalSignature> digitalSignatures = new ArrayList<>();
                User sender = User.builder()
                        .id(senderInfo.getId())
                        .email(senderInfo.getEmail())
                        .build();
                DigitalSignature senderDigital = DigitalSignature.builder()
                        .url(urlSignature)
                        .userUUID(sender.getId())
                        .status("CHOKY")
                        .build();
                digitalSignatures.add(senderDigital);

                List<Receiver> receivers = clientRequest.getReceivers();
                for (Receiver receiver : receivers) {
                    User user = User.builder()
                            .id(UUID.randomUUID())
                            .email(receiver.getEmail())
                            .build(); //proxy.getUserByEmail(receiver.getEmail());

                    DigitalSignature userDS = DigitalSignature.builder()
                            .userUUID(user.getId())
                            .status("CANKY")
                            .build();

                    digitalSignatures.add(userDS);
                }

                Document document = Document.builder()
                        .url(urlDocument)
                        .sentDate(new Date())
                        .digitalSignatures(digitalSignatures)
                        .build();
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
    public boolean uploadFiles(List<String> pathList) {
        pathList.forEach(filePath -> {
            try {
                byte[] bytes = Files.readAllBytes(Path.of(filePath));
                azureStorageService.uploadOverride("???", bytes);
            } catch (IOException e) {
                log.error("Can't read file path {}", filePath);
                e.printStackTrace();
            }
        });
        return false;
    }
}
