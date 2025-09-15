package com.loopers.application.payment;

// =============================================================================
// PaymentProcessorFactory - 결제 수단별 Processor 선택
// =============================================================================

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class PaymentProcessorFactory {
    private final CardPaymentProcessor cardPaymentProcessor;
    private final PointPaymentProcessor pointPaymentProcessor;

    public CardPaymentProcessor card() {
        return cardPaymentProcessor;
    }

    public PointPaymentProcessor point() {
        return pointPaymentProcessor;
    }
}
