package com.synapse.platform.audit.kafka.consumer;

import com.synapse.platform.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * audit는 모든 도메인 이벤트를 수신해 감사 로그로 남깁니다.
 * 토픽 패턴 매칭으로 `synapse.*` 전 도메인 이벤트를 한 컨슈머로 흡수.
 */
@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);

    private final AuditService auditService;

    public AuditEventConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(
        topicPattern = "synapse\\..*",
        groupId = "synapse-platform-audit",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onAnyEvent(Object event) {
        log.info("Audit captured event: type={}", event.getClass().getSimpleName());
        // TODO: auditService.record(event) — payload 직렬화 + actorId 추출 + DB 저장
    }
}
