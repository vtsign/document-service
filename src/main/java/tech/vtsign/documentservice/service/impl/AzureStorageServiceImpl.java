package tech.vtsign.documentservice.service.impl;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.service.AzureStorageService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageServiceImpl implements AzureStorageService {

    private final BlobContainerClient blobContainerClient;

    @Override
    public String upload(String fileName, byte[] data, boolean override) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        try {
            blobClient.upload(BinaryData.fromBytes(data), override);
            return blobClient.getBlobUrl();
        } catch (Exception ex) {
            log.error("Upload file error {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
