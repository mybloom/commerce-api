package com.loopers.support.error;

public class CardPaymentException extends RuntimeException {
    public CardPaymentException(String message) {
        super(message);
    }

    public CardPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
