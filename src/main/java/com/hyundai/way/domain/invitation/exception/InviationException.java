package com.hyundai.way.domain.invitation.exception;

import com.hyundai.way.global.exception.BusinessException;

public class InviationException extends BusinessException {
    public InviationException(InviationErrorCode errorCode) {
        super(errorCode);
    }
}
