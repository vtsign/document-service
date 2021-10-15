package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.model.User;
import tech.vtsign.documentservice.service.DocumentProducer;


@Service
@RequiredArgsConstructor
public class DocumentProducerImpl implements DocumentProducer {
    private static final String TOPIC = "final-topic";


    private final KafkaTemplate<String, User> kafkaTemplate;


    @Override
    public void sendMessage(User user) {
        Message<User> message = MessageBuilder
                .withPayload(user)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        this.kafkaTemplate.send(message);
    }
}
