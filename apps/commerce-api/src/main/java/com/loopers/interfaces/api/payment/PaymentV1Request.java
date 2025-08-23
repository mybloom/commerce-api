package com.loopers.interfaces.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.loopers.application.payment.PaymentInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentV1Request {

    // ─────────────────────────────────────────────────────────────────────────
    // 공통 추상 타입: paymentMethod 로 서브타입을 고르는 다형성 설정
    // ─────────────────────────────────────────────────────────────────────────
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY, // 기존 속성 사용
            property = "paymentMethod",
            visible = true // 서브타입 생성자에서도 읽을 수 있게
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = CardPay.class,  name = "CARD"),
            @JsonSubTypes.Type(value = PointPay.class, name = "POINT")
    })
    public static abstract class Pay {
        @NotNull(message = "주문 ID는 필수입니다")
        @Positive
        private final Long orderId;

        @NotNull(message = "결제 수단은 필수입니다")
        private final String paymentMethod;

        public abstract PaymentInfo.Pay convertToCommand(Long userId);

    }

    // ─────────────────────────────────────────────────────────────────────────
    // 카드 결제
    // ─────────────────────────────────────────────────────────────────────────
    @Getter
    public static class CardPay extends Pay {
        @NotNull(message = "카드 번호는 필수입니다")
        @NotBlank(message = "카드 번호는 공백일 수 없습니다")
        private final String cardNumber;

        @NotNull(message = "카드 타입은 필수입니다")
        @NotBlank(message = "카드 타입은 공백일 수 없습니다")
        private final String cardType;

        // JSON 역직렬화용 생성자 (amount 제거!)
        @JsonCreator
        public CardPay(
                @JsonProperty("orderId") Long orderId,
                @JsonProperty("paymentMethod") String paymentMethod, // "CARD"
                @JsonProperty("cardNumber") String cardNumber,
                @JsonProperty("cardType") String cardType
        ) {
            super(orderId, "CARD"); // 서버에서 고정
            this.cardNumber = cardNumber;
            this.cardType = cardType;
        }

        @Override
        public PaymentInfo.Pay convertToCommand(Long userId) {
            return PaymentInfo.CardPay.of(
                    userId,
                    this.getOrderId(),
                    this.getCardNumber(),
                    this.getCardType()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 포인트 결제
    // ─────────────────────────────────────────────────────────────────────────
    @Getter
    public static class PointPay extends Pay {

        // JSON 역직렬화용 생성자
        @JsonCreator
        public PointPay(
                @JsonProperty("orderId") Long orderId,
                @JsonProperty("paymentMethod") String paymentMethod // "POINT"
        ) {
            super(orderId, "POINT"); // 서버에서 고정
        }

        @Override
        public PaymentInfo.Pay convertToCommand(Long userId) {
            return PaymentInfo.PointPay.of(
                    userId,
                    this.getOrderId()
            );
        }
    }
}
