package com.loopers.support.error.pg;

import com.loopers.support.error.ErrorType;

public class PgTransientException extends PgGatewayException {
    public PgTransientException(String result, String errorCode, String message, Throwable cause)  {
        super(ErrorType.INTERNAL_ERROR, result, errorCode, message, cause);
    }
}
