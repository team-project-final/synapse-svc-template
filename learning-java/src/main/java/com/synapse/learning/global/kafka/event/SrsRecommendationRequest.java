package com.synapse.learning.global.kafka.event;

import java.time.Instant;

public record SrsRecommendationRequest(
    String requestId,
    Long userId,
    String context,
    int topK,
    Instant requestedAt
) {
}
