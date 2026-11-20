package com.hyundai.way.domain.vehicle.exception;

import com.hyundai.way.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VehicleErrorCode implements ErrorCode {
    VEHICLE_NOT_REGISTERED(HttpStatus.FORBIDDEN, "VEHICLE_NOT_REGISTERED", "등록되지 않은 차량입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
