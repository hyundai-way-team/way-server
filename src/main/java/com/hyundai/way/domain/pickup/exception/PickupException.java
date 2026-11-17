package com.hyundai.way.domain.pickup.exception;

import com.hyundai.way.global.exception.BusinessException;

public final class PickupException extends BusinessException {

    public PickupException(PickupErrorCode errorCode) {
        super(errorCode);
    }
}
