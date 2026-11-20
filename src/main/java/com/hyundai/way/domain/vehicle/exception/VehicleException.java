package com.hyundai.way.domain.vehicle.exception;

import com.hyundai.way.global.exception.BusinessException;

public final class VehicleException extends BusinessException {

    public VehicleException(VehicleErrorCode errorCode) {
        super(errorCode);
    }
}
