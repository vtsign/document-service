package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.model.User;

public interface DocumentProducer {
    void sendMessage(User user);
}
