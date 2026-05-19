package com.synapse.platform.notification.infrastructure.messaging;

import com.synapse.platform.global.kafka.event.UserRegistered;
import com.synapse.platform.notification.api.dto.request.SendNotificationRequest;
import com.synapse.platform.notification.application.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class UserRegisteredKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredKafkaConsumer.class);

    private final NotificationService notificationService;

    UserRegisteredKafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = "synapse.platform.auth.user-registered.v1",
        groupId = "synapse-platform-notification"
    )
    public void onUserRegistered(UserRegistered event) {
        log.info("UserRegistered → 환영 알림: userId={}", event.userId());
        notificationService.send(new SendNotificationRequest(event.userId(), "EMAIL", "welcome:" + event.email()));
    }
}
