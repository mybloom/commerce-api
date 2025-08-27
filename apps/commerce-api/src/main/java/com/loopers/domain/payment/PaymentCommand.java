package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;

public class PaymentCommand {

    public record SaveFail(
            Long userId,
            Long orderId,
            PaymentMethod paymentMethod,
            Money amount,
            String paymentFailureReason
    ) {
        public static SaveFail of(Long userId, Long orderId, PaymentMethod paymentMethod, Money amount,
                                  String paymentFailureReason) {
            return new SaveFail(userId, orderId, paymentMethod, amount, paymentFailureReason);
        }
    }

    public record SaveCard(
            Long userId,
            String transactionKey,
            Long orderId,
            PaymentMethod paymentMethod,
            Money amount
    ) {
        public static SaveCard of(
                Long userId,
                String transactionKey,
                Long orderId,
                Money amount
        ) {
            return new SaveCard(
                    userId,
                    transactionKey,
                    orderId,
                    PaymentMethod.CARD,
                    amount);
        }
    }
}
