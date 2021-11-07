package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.service.DocumentProducer;


@Service
@RequiredArgsConstructor
public class DocumentProducerImpl implements DocumentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
//    @Value("${tech.vtsign.kafka.document-service.notify-sign}")
//    private String TOPIC;

    @Override
    public void sendMessage(Object object, String topic) {
        Message<Object> message = MessageBuilder
                .withPayload(object)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        this.kafkaTemplate.send(message);
    }
}
