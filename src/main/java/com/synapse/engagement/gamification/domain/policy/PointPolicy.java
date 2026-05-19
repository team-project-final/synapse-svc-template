package com.synapse.engagement.gamification.domain.policy;

/**
 * 활동 → 점수 매핑 정책. engagement의 핵심 비즈니스 룰이 여기에 모입니다.
 * "댓글 10점, 노트 20점, 복습은 quality × 5점" 같은 결정이 이 한 파일에.
 *
 * 운영 중 점수를 자주 조정하지만, 이 파일만 수정하면 끝.
 * 단위 테스트도 Spring 없이 가능.
 */
public final class PointPolicy {

    public static final String REASON_COMMENT = "COMMENT";
    public static final String REASON_NOTE_WRITE = "NOTE_WRITE";
    public static final String REASON_CARD_REVIEW = "CARD_REVIEW";
    public static final String REASON_FIRST_LOGIN = "FIRST_LOGIN";

    private PointPolicy() {}

    public static long pointsFor(String reason, int rawValue) {
        return switch (reason) {
            case REASON_COMMENT -> 10;
            case REASON_NOTE_WRITE -> 20;
            case REASON_CARD_REVIEW -> rawValue * 5L;
            case REASON_FIRST_LOGIN -> 100;
            default -> 0;
        };
    }
}
