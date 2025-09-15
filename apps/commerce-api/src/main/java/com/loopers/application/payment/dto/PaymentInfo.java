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
    //PaymentInfo.Pay가 sealed이면 switch가 컴파일 타임에 누락 케이스를 잡아준다.
    //permits 명시: Pay의 서브타입을 모두 permits에 나열해야 함.
    //sealed 클래스는 abstract, final, non-sealed 중 하나여야 함.
    //sealed 클래스로 안하고 usecase에서 switch 시에 default 처리 해도 된다.
    public sealed static abstract class Pay
            permits CardPay, PointPay{
        private final Long userId;
        private final Long orderId;
        private final PaymentMethod paymentMethod;
    }

    // =============================================================================
    // 카드 결제 정보
    // =============================================================================
    @Getter
    public static final class CardPay extends Pay {
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
    public static final class PointPay extends Pay {

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
