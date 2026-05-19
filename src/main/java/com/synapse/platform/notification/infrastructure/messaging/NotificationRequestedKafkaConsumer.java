package com.synapse.platform.notification.infrastructure.messaging;

import com.synapse.platform.global.kafka.event.NotificationRequested;
import com.synapse.platform.notification.api.dto.request.SendNotificationRequest;
import com.synapse.platform.notification.application.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class NotificationRequestedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRequestedKafkaConsumer.class);
    public static final String TOPIC = "synapse.platform.notification.requested.v1";

    private final NotificationService notificationService;

    NotificationRequestedKafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = TOPIC, groupId = "synapse-platform-notification")
    public void onNotificationRequested(NotificationRequested event) {
        log.info("NotificationRequested: userId={}, channel={}", event.userId(), event.channel());
        notificationService.send(new SendNotificationRequest(event.userId(), event.channel(), event.payload()));
    }
}
