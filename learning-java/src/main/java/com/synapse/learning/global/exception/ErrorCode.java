package com.synapse.learning.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

    // card
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "CD001", "카드를 찾을 수 없습니다."),
    CARD_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "CD002", "카드 앞/뒤면 텍스트가 필요합니다."),

    // srs
    INVALID_REVIEW_QUALITY(HttpStatus.BAD_REQUEST, "SR001", "복습 점수는 0~5 사이여야 합니다.");

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
