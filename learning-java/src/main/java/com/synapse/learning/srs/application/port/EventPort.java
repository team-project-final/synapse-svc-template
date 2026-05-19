package com.synapse.learning.srs.application.port;

import com.synapse.learning.global.kafka.event.CardReviewed;
import com.synapse.learning.global.kafka.event.SrsRecommendationRequest;

public interface EventPort {
    void publishCardReviewed(CardReviewed event);
    void publishRecommendationRequest(SrsRecommendationRequest event);
}
