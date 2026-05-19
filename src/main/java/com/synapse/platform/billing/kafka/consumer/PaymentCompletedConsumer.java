package com.synapse.platform.billing.kafka.consumer;

import com.synapse.platform.billing.service.BillingService;
import com.synapse.platform.global.kafka.event.PaymentCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedConsumer.class);
    public static final String TOPIC = "synapse.external.pg.payment-completed.v1";

    private final BillingService billingService;

    public PaymentCompletedConsumer(BillingService billingService) {
        this.billingService = billingService;
    }

    @KafkaListener(topics = TOPIC, groupId = "synapse-platform-billing")
    public void onPaymentCompleted(PaymentCompleted event) {
        log.info("PaymentCompleted received: invoiceId={}", event.invoiceId());
        // TODO: billingService.markPaid(event)
    }
}
