package com.synapse.platform.billing.api.dto.response;

import java.math.BigDecimal;

public record InvoiceResponse(Long id, Long userId, BigDecimal amount, String status) {
}
