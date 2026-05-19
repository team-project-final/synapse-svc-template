package com.synapse.platform.billing.domain.policy;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 결제 가능 정책 — 도메인 룰. 외부 의존성 없음.
 */
public final class ChargePolicy {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("100");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");
    private static final Set<String> ALLOWED_CURRENCIES = Set.of("KRW", "USD", "JPY", "EUR");

    private ChargePolicy() {}

    public static boolean isChargeable(BigDecimal amount, String currency) {
        if (amount == null || currency == null) return false;
        if (!ALLOWED_CURRENCIES.contains(currency)) return false;
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }
}
