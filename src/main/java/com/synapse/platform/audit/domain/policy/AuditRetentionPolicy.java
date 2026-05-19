package com.synapse.platform.audit.domain.policy;

import java.time.Duration;
import java.time.Instant;

/**
 * 감사 로그 보존 정책 — 도메인 룰.
 */
public final class AuditRetentionPolicy {

    private static final Duration RETENTION = Duration.ofDays(365 * 7); // 7년

    private AuditRetentionPolicy() {}

    public static boolean shouldPurge(Instant occurredAt, Instant now) {
        return Duration.between(occurredAt, now).compareTo(RETENTION) > 0;
    }
}
