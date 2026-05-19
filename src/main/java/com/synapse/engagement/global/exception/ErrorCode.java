package com.synapse.engagement.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

    // community
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COM001", "게시글을 찾을 수 없습니다."),
    COMMENT_BODY_BLANK(HttpStatus.BAD_REQUEST, "COM002", "댓글 본문이 비어있습니다."),

    // gamification
    USER_SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "GAM001", "사용자 점수 정보를 찾을 수 없습니다.");

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
