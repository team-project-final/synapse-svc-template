package com.synapse.engagement.gamification.infrastructure.messaging;

import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.global.kafka.event.UserRegistered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class UserRegisteredKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredKafkaConsumer.class);

    private final GamificationService gamificationService;

    UserRegisteredKafkaConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.platform.auth.user-registered.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onUserRegistered(UserRegistered event) {
        log.info("UserRegistered → EARLY_ADOPTER 뱃지: userId={}", event.userId());
        gamificationService.awardBadge(event.userId(), "EARLY_ADOPTER");
    }
}
