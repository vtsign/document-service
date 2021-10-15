package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.model.DocumentClientRequest;

import java.util.List;

public interface DocumentService {
    boolean createDigitalSignature(DocumentClientRequest clientRequest, List<MultipartFile> files);
    boolean uploadFiles(List<String>pathList);

//    // will return signature byte[]
//    byte[] createSignature(MultipartFile file, String privateKeyUrl) throws IOException;
//    // will return file url;
//    String uploadFile(String name, byte[] bytes);

}
