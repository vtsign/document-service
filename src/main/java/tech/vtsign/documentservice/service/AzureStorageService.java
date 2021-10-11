package tech.vtsign.documentservice.service;

public interface AzureStorageService {
    String upload(String fileName, byte[] data, boolean override);
    default String uploadNotOverride(String fileName, byte[] data) {
       return upload(fileName, data, false);
    }
    default String uploadOverride(String fileName, byte[] data) {
        return upload(fileName, data, true);
    }
}
