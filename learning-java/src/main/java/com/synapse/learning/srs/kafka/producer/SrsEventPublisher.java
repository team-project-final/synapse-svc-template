package com.synapse.learning.srs.kafka.producer;

import com.synapse.learning.global.kafka.event.CardReviewed;
import com.synapse.learning.global.kafka.event.SrsRecommendationRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SrsEventPublisher {

    public static final String TOPIC_CARD_REVIEWED = "synapse.learning.card.card-reviewed.v1";
    public static final String TOPIC_RECOMMENDATION_REQUEST = "synapse.learning.srs.recommendation-request.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SrsEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCardReviewed(CardReviewed event) {
        kafkaTemplate.send(TOPIC_CARD_REVIEWED, event.userId().toString(), event);
    }

    public void publishRecommendationRequest(SrsRecommendationRequest event) {
        kafkaTemplate.send(TOPIC_RECOMMENDATION_REQUEST, event.userId().toString(), event);
    }
}
