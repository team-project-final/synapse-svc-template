package com.synapse.platform.global.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompleted(
    String idempotencyKey,
    Long userId,
    Long invoiceId,
    BigDecimal amount,
    Instant completedAt
) {
}
