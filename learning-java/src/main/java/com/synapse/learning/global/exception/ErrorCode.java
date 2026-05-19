package com.synapse.learning.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 에러 코드에 서비스 prefix `L_` (Learning) 적용.
 * 다른 서비스(platform=P_, knowledge=KN_, engagement=ENG_)의 코드와 충돌 방지.
 */
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "L_C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "L_C002", "인증이 필요합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "L_C004", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "L_C999", "서버 오류가 발생했습니다."),

    // card
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "L_CD001", "카드를 찾을 수 없습니다."),
    CARD_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "L_CD002", "카드 앞/뒤면 텍스트가 필요합니다."),

    // srs
    INVALID_REVIEW_QUALITY(HttpStatus.BAD_REQUEST, "L_SR001", "복습 점수는 0~5 사이여야 합니다.");

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
