package com.synapse.learning.srs.infrastructure.messaging;

import com.synapse.learning.global.kafka.event.CardReviewed;
import com.synapse.learning.global.kafka.event.SrsRecommendationRequest;
import com.synapse.learning.srs.application.port.EventPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class SrsEventKafkaAdapter implements EventPort {

    public static final String TOPIC_CARD_REVIEWED = "synapse.learning.card.card-reviewed.v1";
    public static final String TOPIC_RECOMMENDATION_REQUEST = "synapse.learning.srs.recommendation-request.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    SrsEventKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishCardReviewed(CardReviewed event) {
        kafkaTemplate.send(TOPIC_CARD_REVIEWED, event.userId().toString(), event);
    }

    @Override
    public void publishRecommendationRequest(SrsRecommendationRequest event) {
        kafkaTemplate.send(TOPIC_RECOMMENDATION_REQUEST, event.userId().toString(), event);
    }
}
