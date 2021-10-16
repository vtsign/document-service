package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.model.DocumentClientRequest;

import java.util.List;

public interface DocumentService {
    boolean createDigitalSignature(DocumentClientRequest clientRequest, List<MultipartFile> files);

//    // will return signature byte[]
    byte[] createSignature(MultipartFile file, String privateKeyUrl) throws Exception;
//    // will return file url;
    String uploadFile(String name, byte[] bytes);



}
