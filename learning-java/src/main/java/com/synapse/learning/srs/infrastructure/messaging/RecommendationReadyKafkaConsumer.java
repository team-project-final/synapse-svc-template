package com.synapse.learning.srs.infrastructure.messaging;

import com.synapse.learning.global.kafka.event.SrsRecommendationReady;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class RecommendationReadyKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RecommendationReadyKafkaConsumer.class);

    @KafkaListener(
        topics = "synapse.learning.ai.recommendation-ready.v1",
        groupId = "synapse-learning-srs"
    )
    public void onRecommendationReady(SrsRecommendationReady event) {
        log.info("RecommendationReady from Python: requestId={}, count={}",
            event.requestId(), event.recommendedCardIds().size());
    }
}
