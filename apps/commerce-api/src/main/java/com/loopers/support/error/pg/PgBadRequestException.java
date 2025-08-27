package com.loopers.support.error.pg;

import com.loopers.support.error.ErrorType;

public class PgBadRequestException extends PgGatewayException {
    public PgBadRequestException(String result, String errorCode, String message, Throwable cause) {
        super(ErrorType.BAD_REQUEST, result, errorCode, message, cause);
    }
}
