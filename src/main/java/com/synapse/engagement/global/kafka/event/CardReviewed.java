package com.synapse.engagement.global.kafka.event;

import java.time.Instant;

public record CardReviewed(Long cardId, Long userId, int quality, Instant reviewedAt) {
}
