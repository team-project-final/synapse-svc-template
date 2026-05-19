package com.synapse.platform.notification.infrastructure.persistence;

import com.synapse.platform.notification.application.port.NotificationPort;
import com.synapse.platform.notification.domain.Notification;
import org.springframework.stereotype.Component;

@Component
class NotificationPersistenceAdapter implements NotificationPort {

    private final NotificationJpaRepository jpaRepository;

    NotificationPersistenceAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }
}
