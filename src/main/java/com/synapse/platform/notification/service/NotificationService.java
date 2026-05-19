package com.synapse.platform.notification.service;

import com.synapse.platform.notification.dto.request.SendNotificationRequest;
import com.synapse.platform.notification.dto.response.NotificationResponse;
import com.synapse.platform.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse send(SendNotificationRequest request) {
        // TODO: 채널 디스패치 (W3에서 Kafka consumer로 이벤트 기반 전환)
        return new NotificationResponse(0L, request.userId(), request.channel(), "QUEUED");
    }
}
