package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.model.DocumentClientRequest;

import java.util.List;

public interface DocumentService {
    boolean createUserDocument(DocumentClientRequest clientRequest, List<MultipartFile> files);

    String uploadFile(String name, byte[] bytes);
}
