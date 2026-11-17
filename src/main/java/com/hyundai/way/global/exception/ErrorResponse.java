package com.hyundai.way.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) @Nullable List<FieldErrorDetail> errors
) {

    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
