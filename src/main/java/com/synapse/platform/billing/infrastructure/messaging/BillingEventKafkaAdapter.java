package com.synapse.platform.billing.infrastructure.messaging;

import com.synapse.platform.billing.application.port.EventPort;
import com.synapse.platform.global.kafka.event.BillingChargeRequested;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class BillingEventKafkaAdapter implements EventPort {

    public static final String TOPIC = "synapse.platform.billing.charge-requested.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    BillingEventKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishChargeRequested(BillingChargeRequested event) {
        kafkaTemplate.send(TOPIC, event.idempotencyKey(), event);
    }
}
