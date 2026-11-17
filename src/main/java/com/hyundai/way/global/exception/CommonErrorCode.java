package com.hyundai.way.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "입력값을 확인해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-0401", "요청을 인증할 수 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-0404", "요청한 항목을 찾을 수 없습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "COMMON-0429", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-0500", "요청 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
