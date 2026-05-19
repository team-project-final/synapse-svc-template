package com.synapse.engagement.gamification.infrastructure.messaging;

import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.domain.policy.PointPolicy;
import com.synapse.engagement.global.kafka.event.CardReviewed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class CardReviewedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CardReviewedKafkaConsumer.class);

    private final GamificationService gamificationService;

    CardReviewedKafkaConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.learning.card.card-reviewed.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onCardReviewed(CardReviewed event) {
        log.info("CardReviewed → 복습 포인트: userId={}, quality={}", event.userId(), event.quality());
        gamificationService.awardForEvent(event.userId(), PointPolicy.REASON_CARD_REVIEW, event.quality());
    }
}
