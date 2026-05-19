package com.synapse.platform.global.kafka.event;

import java.time.Instant;

public record UserRegistered(
    Long userId,
    String email,
    Instant registeredAt
) {
}
