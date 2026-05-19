package com.synapse.platform.notification.infrastructure.persistence;

import com.synapse.platform.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
}
