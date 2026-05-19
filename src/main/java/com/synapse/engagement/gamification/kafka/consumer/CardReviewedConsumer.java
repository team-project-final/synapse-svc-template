package com.synapse.engagement.gamification.kafka.consumer;

import com.synapse.engagement.gamification.service.GamificationService;
import com.synapse.engagement.global.kafka.event.CardReviewed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * learning 서비스의 카드 복습 이벤트 → 학습 포인트.
 */
@Component
public class CardReviewedConsumer {

    private static final Logger log = LoggerFactory.getLogger(CardReviewedConsumer.class);

    private final GamificationService gamificationService;

    public CardReviewedConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.learning.card.card-reviewed.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onCardReviewed(CardReviewed event) {
        log.info("CardReviewed → 복습 포인트: userId={}, quality={}", event.userId(), event.quality());
        gamificationService.awardPoint(event.userId(), event.quality() * 5L, "CARD_REVIEW");
    }
}
