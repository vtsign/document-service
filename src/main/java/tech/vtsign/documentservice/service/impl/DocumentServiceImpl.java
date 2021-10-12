package tech.vtsign.documentservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Override
    public boolean createDigitalSignature(MultipartFile file) {

        
        return false;
    }

    @Override
    public boolean uploadFiles(List<String> pathList) {
        return false;
    }
}
