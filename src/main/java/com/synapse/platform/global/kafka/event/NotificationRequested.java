package com.synapse.platform.global.kafka.event;

import java.time.Instant;

public record NotificationRequested(
    Long userId,
    String channel,
    String templateKey,
    String payload,
    Instant requestedAt
) {
}
