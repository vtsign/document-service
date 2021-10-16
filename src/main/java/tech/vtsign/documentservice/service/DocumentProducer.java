package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.model.InfoMailReceiver;

public interface DocumentProducer {
    void sendMessage(InfoMailReceiver infoMailReceiver);
}
