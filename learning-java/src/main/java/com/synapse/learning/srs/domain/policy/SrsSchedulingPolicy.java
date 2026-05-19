package com.synapse.learning.srs.domain.policy;

/**
 * Spaced Repetition 스케줄링 정책 (SM-2 단순화).
 * quality(0~5) → 다음 복습까지 일수.
 * 알고리즘 변경 시 이 파일만 수정.
 */
public final class SrsSchedulingPolicy {

    private SrsSchedulingPolicy() {}

    public static long nextIntervalDays(int quality) {
        if (quality < 3) return 1;
        return (long) Math.pow(2, quality - 2);
    }
}
