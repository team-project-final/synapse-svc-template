package com.synapse.platform.notification.application.port;

import com.synapse.platform.notification.domain.Notification;

public interface NotificationPort {
    Notification save(Notification notification);
}
