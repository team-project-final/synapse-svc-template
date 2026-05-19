package com.synapse.platform.billing.kafka.producer;

import com.synapse.platform.global.kafka.event.BillingChargeRequested;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BillingEventPublisher {

    public static final String TOPIC_CHARGE_REQUESTED = "synapse.platform.billing.charge-requested.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BillingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishChargeRequested(BillingChargeRequested event) {
        kafkaTemplate.send(TOPIC_CHARGE_REQUESTED, event.idempotencyKey(), event);
    }
}
