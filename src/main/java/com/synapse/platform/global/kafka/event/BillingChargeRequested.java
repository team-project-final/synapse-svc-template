package com.synapse.platform.global.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;

public record BillingChargeRequested(
    String idempotencyKey,
    Long userId,
    BigDecimal amount,
    String currency,
    Instant requestedAt
) {
}
