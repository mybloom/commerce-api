package com.loopers.application.payment.dto;

import com.loopers.domain.commonvo.Money;

public class PaymentCallbackInfo {
    public record ProcessTransaction(
            Long orderId,
            Money amount,
            String transactionKey,
            PgProcessStatus status,
            String reason
    ) {
        public ProcessTransaction(String orderId, Long amount, String transactionKey, String status, String reason) {
            this(
                    Long.parseLong(orderId),
                    Money.of(amount),
                    transactionKey,
                    PgProcessStatus.valueOf(status.toUpperCase()),
                    reason
            );
        }
    }
}
