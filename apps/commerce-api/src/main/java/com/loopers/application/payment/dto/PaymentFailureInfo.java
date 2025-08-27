package com.loopers.application.payment.dto;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;

public class PaymentFailureInfo {
    public record Fail(
            Long userId,
            PaymentMethod paymentMethod,
            Long orderId,
            Money amount,
            String paymentFailureReason
    ) {
        public static Fail of(PaymentInfo.Pay info, Money amount, String paymentFailureReason) {
            return new Fail(info.getUserId(), info.getPaymentMethod(), info.getOrderId(), amount, paymentFailureReason);
        }

        public PaymentCommand.SaveFail convertToCommand() {
            return PaymentCommand.SaveFail.of(
                    this.userId,
                    this.orderId,
                    this.paymentMethod,
                    this.amount,
                    this.paymentFailureReason
            );
        }
    }

    public record CardPgFail(
            Long orderId,
            String transactionKey,
            PgProcessStatus status,
            String failureReason
    ) {
        public static PaymentFailureInfo.CardPgFail of(
                Long orderId, String transactionKey, PgProcessStatus status, String failureReason
        ) {
            return new PaymentFailureInfo.CardPgFail(
                    orderId, transactionKey, status, failureReason);
        }
    }

    public record CardCallbackFail(
            Long orderId,
            String transactionKey,
            PgProcessStatus status,
            String failureReason
    ) {
        public static CardCallbackFail of(PaymentCallbackInfo.ProcessTransaction info) {
            return new CardCallbackFail(
                    info.orderId(),
                    info.transactionKey(),
                    info.status(),
                    info.reason()
            );
        }
    }
}
