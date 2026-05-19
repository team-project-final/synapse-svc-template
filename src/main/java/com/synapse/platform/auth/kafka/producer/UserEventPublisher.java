package com.synapse.platform.auth.kafka.producer;

import com.synapse.platform.global.kafka.event.UserRegistered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    public static final String TOPIC_USER_REGISTERED = "synapse.platform.auth.user-registered.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(UserRegistered event) {
        log.info("Publishing UserRegistered: userId={}", event.userId());
        kafkaTemplate.send(TOPIC_USER_REGISTERED, event.userId().toString(), event);
    }
}
