package com.hyundai.way.domain.pickup.exception;

import com.hyundai.way.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PickupErrorCode implements ErrorCode {


    INVITATION_UNAVAILABLE(HttpStatus.FORBIDDEN, "INVITATION_UNAVAILABLE", "사용할 수 없는 초대입니다."),
    INVITATION_LINK_UNAVAILABLE(HttpStatus.CONFLICT, "INVITATION_LINK_UNAVAILABLE", "기존 초대 링크를 복구할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
