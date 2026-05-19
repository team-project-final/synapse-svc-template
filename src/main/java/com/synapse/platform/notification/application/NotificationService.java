package com.synapse.platform.notification.application;

import com.synapse.platform.global.exception.BusinessException;
import com.synapse.platform.global.exception.ErrorCode;
import com.synapse.platform.notification.api.dto.request.SendNotificationRequest;
import com.synapse.platform.notification.api.dto.response.NotificationResponse;
import com.synapse.platform.notification.application.port.NotificationPort;
import com.synapse.platform.notification.domain.Notification;
import com.synapse.platform.notification.domain.policy.NotificationChannelPolicy;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationPort notificationPort;

    public NotificationService(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    public NotificationResponse send(SendNotificationRequest request) {
        if (!NotificationChannelPolicy.isSupported(request.channel())) {
            throw new BusinessException(ErrorCode.NOTIFICATION_CHANNEL_INVALID);
        }
        Notification saved = notificationPort.save(new Notification(request.userId(), request.channel(), "QUEUED"));
        return new NotificationResponse(saved.getId(), saved.getUserId(), saved.getChannel(), saved.getStatus());
    }
}
