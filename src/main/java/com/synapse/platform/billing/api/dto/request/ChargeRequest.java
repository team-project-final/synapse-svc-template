package com.synapse.platform.billing.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ChargeRequest(
    @NotNull Long userId,
    @NotNull @Positive BigDecimal amount,
    @NotNull String currency
) {
}
