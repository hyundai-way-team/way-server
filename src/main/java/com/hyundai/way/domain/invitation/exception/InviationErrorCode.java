package com.hyundai.way.domain.invitation.exception;

import com.hyundai.way.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InviationErrorCode implements ErrorCode {

    ALREADY_CONSUMED_CODE(HttpStatus.BAD_REQUEST, "ALREADY_CONSUMED_CODE", "이미 사용이 완료된 코드입니다."),

    INVITATION_UNAVAILABLE_CODE(HttpStatus.FORBIDDEN, "INVITATION_UNAVAILABLE_CODE", "등록되지 않은 초대 코드입니다. 올바른 코드를 입력해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
