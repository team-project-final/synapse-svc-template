package com.synapse.knowledge.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

    // note
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "노트를 찾을 수 없습니다."),
    NOTE_TITLE_BLANK(HttpStatus.BAD_REQUEST, "N002", "노트 제목은 비어있을 수 없습니다."),

    // graph
    NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "노드를 찾을 수 없습니다."),
    EDGE_SELF_LOOP(HttpStatus.BAD_REQUEST, "G002", "자기 자신을 연결할 수 없습니다."),

    // chunking
    CHUNK_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "청크 작업을 찾을 수 없습니다.");

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
