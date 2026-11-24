package com.hyundai.way.domain.invitation.exception;

import com.hyundai.way.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InviationErrorCode implements ErrorCode {

    ALREADY_CONSUMED_CODE(HttpStatus.FORBIDDEN, "ALREADY_CONSUMED_CODE", "이미 사용이 완료된 코드입니다."),

    INVITATION_UNAVAILABLE(HttpStatus.FORBIDDEN, "INVITATION_UNAVAILABLE", "사용할 수 없는 초대입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
