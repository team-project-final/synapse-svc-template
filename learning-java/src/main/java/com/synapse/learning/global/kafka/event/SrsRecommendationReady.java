package com.synapse.learning.global.kafka.event;

import java.time.Instant;
import java.util.List;

public record SrsRecommendationReady(
    String requestId,
    Long userId,
    List<Long> recommendedCardIds,
    Instant readyAt
) {
}
