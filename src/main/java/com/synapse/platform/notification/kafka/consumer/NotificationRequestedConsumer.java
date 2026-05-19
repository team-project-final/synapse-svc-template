package com.synapse.platform.notification.kafka.consumer;

import com.synapse.platform.global.kafka.event.NotificationRequested;
import com.synapse.platform.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 외부 서비스(knowledge/engagement/learning)가 발행하는 일반 알림 요청 토픽.
 */
@Component
public class NotificationRequestedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRequestedConsumer.class);
    public static final String TOPIC = "synapse.notification.requested.v1";

    private final NotificationService notificationService;

    public NotificationRequestedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = TOPIC, groupId = "synapse-platform-notification")
    public void onNotificationRequested(NotificationRequested event) {
        log.info("NotificationRequested: userId={}, channel={}", event.userId(), event.channel());
        // TODO: notificationService.dispatch(event)
    }
}
