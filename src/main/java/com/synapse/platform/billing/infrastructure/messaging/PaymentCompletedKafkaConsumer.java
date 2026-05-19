package com.synapse.platform.billing.infrastructure.messaging;

import com.synapse.platform.billing.application.port.InvoicePort;
import com.synapse.platform.global.kafka.event.PaymentCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class PaymentCompletedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedKafkaConsumer.class);
    public static final String TOPIC = "synapse.external.pg.payment-completed.v1";

    private final InvoicePort invoicePort;

    PaymentCompletedKafkaConsumer(InvoicePort invoicePort) {
        this.invoicePort = invoicePort;
    }

    @KafkaListener(topics = TOPIC, groupId = "synapse-platform-billing")
    public void onPaymentCompleted(PaymentCompleted event) {
        log.info("PaymentCompleted: invoiceId={}", event.invoiceId());
        invoicePort.findById(event.invoiceId()).ifPresent(invoice -> {
            invoice.markPaid();
            invoicePort.save(invoice);
        });
    }
}
