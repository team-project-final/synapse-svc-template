package com.synapse.platform.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 에러 코드에 서비스 prefix `P_` (Platform) 적용.
 * 다른 서비스(knowledge=KN_, learning=L_, engagement=ENG_)의 코드와 충돌 방지.
 * 클라이언트가 응답 코드만 보고 어느 서비스에서 발생했는지 식별 가능.
 */
public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "P_C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "P_C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "P_C003", "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "P_C004", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "P_C999", "서버 오류가 발생했습니다."),

    // auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "P_A001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "P_A002", "토큰이 만료되었습니다."),

    // billing
    INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "P_B001", "청구서를 찾을 수 없습니다."),

    // notification
    NOTIFICATION_CHANNEL_INVALID(HttpStatus.BAD_REQUEST, "P_N001", "지원하지 않는 알림 채널입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
