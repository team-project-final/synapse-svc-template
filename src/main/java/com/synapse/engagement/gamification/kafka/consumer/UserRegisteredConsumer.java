package com.synapse.engagement.gamification.kafka.consumer;

import com.synapse.engagement.gamification.service.GamificationService;
import com.synapse.engagement.global.kafka.event.UserRegistered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * platform 서비스의 회원가입 이벤트를 수신해 EARLY_ADOPTER 뱃지 부여.
 */
@Component
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final GamificationService gamificationService;

    public UserRegisteredConsumer(GamificationService gamificationService) {
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
