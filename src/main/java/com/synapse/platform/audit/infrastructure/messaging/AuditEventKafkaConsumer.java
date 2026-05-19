package com.synapse.platform.audit.infrastructure.messaging;

import com.synapse.platform.audit.application.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class AuditEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventKafkaConsumer.class);

    private final AuditService auditService;

    AuditEventKafkaConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(
        topicPattern = "synapse\\..*",
        groupId = "synapse-platform-audit"
    )
    public void onAnyEvent(Object event) {
        log.info("Audit captured event: type={}", event.getClass().getSimpleName());
        auditService.record(event.getClass().getSimpleName(), 0L);
    }
}
