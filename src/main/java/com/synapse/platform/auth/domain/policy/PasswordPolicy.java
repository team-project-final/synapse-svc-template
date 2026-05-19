package com.synapse.platform.auth.domain.policy;

/**
 * 비밀번호 정책 — 도메인 룰. 외부 의존성 없음.
 * 정책 변경 시 인프라/API 코드 수정 없이 이 파일만 수정.
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 72; // BCrypt 한계

    private PasswordPolicy() {}

    public static boolean isValid(String raw) {
        if (raw == null) return false;
        int len = raw.length();
        if (len < MIN_LENGTH || len > MAX_LENGTH) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : raw.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
