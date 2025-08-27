package com.loopers.application.payment.dto;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInfo {

    // 공통 결제 정보
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static abstract class Pay {
        private final Long userId;
        private final Long orderId;
        private final PaymentMethod paymentMethod;
    }

    // =============================================================================
    // 카드 결제 정보
    // =============================================================================
    @Getter
    public static class CardPay extends Pay {
        private final String cardNumber;
        private final String cardType;

        @Builder(access = AccessLevel.PRIVATE)
        private CardPay(Long userId, Long orderId, String cardNumber, String cardType) {
            super(userId, orderId, PaymentMethod.CARD);
            this.cardNumber = cardNumber;
            this.cardType = cardType;
        }

        public static CardPay of(Long userId, Long orderId, String cardNumber, String cardType) {
            return CardPay.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .cardNumber(cardNumber)
                    .cardType(cardType)
                    .build();
        }

        public PaymentCommand.SaveCard convertToCommand(Money amount, String transactionKey) {
            return PaymentCommand.SaveCard.of(
                    this.getUserId(),
                    transactionKey,
                    this.getOrderId(),
                    amount
            );

        }

    }

    // =============================================================================
    // 포인트 결제 정보
    // =============================================================================
    @Getter
    public static class PointPay extends Pay {

        @Builder(access = AccessLevel.PRIVATE)
        private PointPay(Long userId, Long orderId) {
            super(userId, orderId, PaymentMethod.POINT);
        }

        public static PointPay of(Long userId, Long orderId) {
            return PointPay.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .build();
        }
    }
}
