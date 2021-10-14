package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    boolean createDigitalSignature(List<MultipartFile> files);
    boolean uploadFiles(List<String>pathList);
}
