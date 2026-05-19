package com.synapse.engagement.gamification.domain.policy;

import java.util.List;

/**
 * 누적 활동 → 뱃지 매핑 정책. 단계별 임계값.
 */
public final class BadgeAchievementPolicy {

    public record Threshold(long minPoints, String badgeCode) {}

    private static final List<Threshold> THRESHOLDS = List.of(
        new Threshold(100, "STARTER"),
        new Threshold(1_000, "ENTHUSIAST"),
        new Threshold(10_000, "EXPERT"),
        new Threshold(100_000, "LEGEND")
    );

    private BadgeAchievementPolicy() {}

    public static String evaluate(long totalPoints) {
        String result = null;
        for (Threshold t : THRESHOLDS) {
            if (totalPoints >= t.minPoints()) result = t.badgeCode();
            else break;
        }
        return result;
    }
}
