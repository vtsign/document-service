package tech.vtsign.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.*;

public interface DocumentService {
    boolean createDigitalSignature(MultipartFile file);
    boolean uploadFiles(List<String>pathList);
}
