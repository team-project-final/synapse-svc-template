package com.synapse.platform.notification.kafka.consumer;

import com.synapse.platform.global.kafka.event.UserRegistered;
import com.synapse.platform.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * notification 도메인은 auth 도메인의 UserRegistered 이벤트를 수신해 환영 알림을 발송.
 * auth.service.AuthService를 직접 호출하지 않고 Kafka 이벤트로만 통신.
 */
@Component
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final NotificationService notificationService;

    public UserRegisteredConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = "synapse.platform.auth.user-registered.v1",
        groupId = "synapse-platform-notification"
    )
    public void onUserRegistered(UserRegistered event) {
        log.info("UserRegistered received: userId={} — 환영 알림 발송 시작", event.userId());
        // TODO: notificationService.sendWelcome(event.userId(), event.email())
    }
}
