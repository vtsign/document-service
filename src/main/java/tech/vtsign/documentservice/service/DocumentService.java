package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.model.DocumentClientRequest;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    boolean createUserDocument(DocumentClientRequest clientRequest, List<MultipartFile> files);

    //    begin NHAN
    boolean createUserDocument2(DocumentClientRequest clientRequest, List<MultipartFile> files);

    //    end NHAN
    String uploadFile(String name, byte[] bytes);

    Document getById(UUID uuid);

}
