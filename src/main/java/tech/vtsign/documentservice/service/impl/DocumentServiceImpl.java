package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.DigitalSignature;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.UserUUID;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.repository.DocumentRepository;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.DocumentService;
import tech.vtsign.documentservice.utils.DSUtil;
import tech.vtsign.documentservice.utils.KeyReaderUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final AzureStorageService azureStorageService;
    private final DocumentRepository documentRepository;

    @Override
    public boolean createDigitalSignature(List<MultipartFile> files) {
        LoginServerResponseDto principal = (LoginServerResponseDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean success = true;
        for(MultipartFile file : files) {
            try {
                PrivateKey privateKey = KeyReaderUtil.getPrivateKey(principal.getPrivateKey());
                byte[] digitalSignature = DSUtil.sign(file.getBytes(),privateKey);
                String urlSignature = azureStorageService.uploadNotOverride(String.format("%s/%s",principal.getId(),principal.getId()+file.getOriginalFilename()),digitalSignature);
                String urlDocument = azureStorageService.uploadNotOverride(String.format("%s",file.getOriginalFilename()), file.getBytes());
                List<DigitalSignature> digitalSignatureList = new ArrayList<>();
                UserUUID userUUID = UserUUID.builder().user_uuid(principal.getId()).build();
                DigitalSignature digitalSignatureClass = DigitalSignature.builder()
                                    .url(urlSignature)
                        .userUUID(userUUID)
                        .build();
                digitalSignatureList.add(digitalSignatureClass);
                Document document = Document
                                        .builder()
                                        .url(urlDocument)
                                        .signedDate(new Date())
                        .sentDate(new Date())
                        .digitalSignatures(digitalSignatureList)
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
