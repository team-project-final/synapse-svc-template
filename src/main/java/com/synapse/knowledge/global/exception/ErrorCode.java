package com.synapse.knowledge.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 에러 코드에 서비스 prefix `KN_` (Knowledge) 적용.
 * 다른 서비스(platform=P_, learning=L_, engagement=ENG_)의 코드와 충돌 방지.
 */
public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "KN_C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "KN_C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "KN_C003", "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "KN_C004", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KN_C999", "서버 오류가 발생했습니다."),

    // note
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "KN_N001", "노트를 찾을 수 없습니다."),
    NOTE_TITLE_BLANK(HttpStatus.BAD_REQUEST, "KN_N002", "노트 제목은 비어있을 수 없습니다."),

    // graph
    NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "KN_G001", "노드를 찾을 수 없습니다."),
    EDGE_SELF_LOOP(HttpStatus.BAD_REQUEST, "KN_G002", "자기 자신을 연결할 수 없습니다."),

    // chunking
    CHUNK_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "KN_CH001", "청크 작업을 찾을 수 없습니다.");

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
