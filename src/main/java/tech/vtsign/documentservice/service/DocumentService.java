package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.model.UserContractResponse;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    boolean createUserDocument(DocumentClientRequest clientRequest, List<MultipartFile> files);

    String uploadFile(String name, byte[] bytes);

    Document getById(UUID uuid);

    UserContractResponse getUDRByContractIdAndUserId(UUID contractUUID, UUID userUUID);
}
