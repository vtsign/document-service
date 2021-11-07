package tech.vtsign.documentservice.service;

public interface DocumentProducer {
    void sendMessage(Object object, String topic);
}
