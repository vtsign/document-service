package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.model.InfoMailReceiver;
import tech.vtsign.documentservice.service.DocumentProducer;


@Service
@RequiredArgsConstructor
public class DocumentProducerImpl implements DocumentProducer {

    private final KafkaTemplate<String, InfoMailReceiver> kafkaTemplate;
    @Value("${tech.vtsign.kafka.document-service.notify-sign}")
    private String TOPIC;

    @Override
    public void sendMessage(InfoMailReceiver infoMailReceiver) {
        Message<InfoMailReceiver> message = MessageBuilder
                .withPayload(infoMailReceiver)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        this.kafkaTemplate.send(message);
    }
}
