package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentV1Response {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PaymentResponse {

        private final Long paymentId;
//        private final String paymentStatus;

        public static PaymentResponse from(PaymentResult.Pay result) {
            return PaymentResponse.builder()
                    .paymentId(result.paymentId())
//                    .paymentStatus(result.)
                    .build();
        }
    }
}
