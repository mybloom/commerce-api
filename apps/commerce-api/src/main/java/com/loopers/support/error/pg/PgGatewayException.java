package com.loopers.support.error.pg;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;


@Getter
public abstract class PgGatewayException extends CoreException {
    private final String result; // PG 응답 코드(ex: FAIL 등)
    private final String errorCode;
    private final String message;    // 에러 메세지

    protected PgGatewayException(ErrorType errorType,
                                 String result,
                                 String errorCode,
                                 String message,
                                 Throwable cause) {
        super(errorType, message); // CoreException(message 포함)

        //원인 예외 체인 보존 //todo: 보존할 필요가 있을까
     /*   if (cause != null && getCause() == null) {
            initCause(cause);
        }*/
        this.result = result != null ? result : "UNKNOWN";
        this.errorCode = errorCode;
        this.message = message;
    }

}

