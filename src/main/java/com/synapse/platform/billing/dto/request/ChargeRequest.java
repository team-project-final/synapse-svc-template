package com.synapse.platform.billing.dto.request;

import java.math.BigDecimal;

public record ChargeRequest(Long userId, BigDecimal amount, String currency) {
}
