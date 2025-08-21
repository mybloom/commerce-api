package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentInfo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentV1Request {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {

        @NotNull(message = "주문 ID는 필수입니다")
        @Positive
        private final Long orderId;

        @NotNull(message = "결제 수단은 필수입니다")
        private final String paymentMethod;

        public PaymentInfo.Pay convertToCommand() {
            return PaymentInfo.Pay.of(orderId, paymentMethod);
        }
    }
}
