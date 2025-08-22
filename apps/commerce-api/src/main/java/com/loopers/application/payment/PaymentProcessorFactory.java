package com.loopers.application.payment;

// =============================================================================
// PaymentProcessorFactory - 결제 수단별 Processor 선택
// =============================================================================

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentProcessorFactory {

    //Spring이 자동으로 PaymentProcessor 구현체들을 주입 -> 어떻게?
    private final List<PaymentProcessor> processors;

    public PaymentProcessor getProcessor(PaymentMethod paymentMethod) {
        return processors.stream()
                .filter(processor -> processor.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST,
                        "지원하지 않는 결제 수단입니다: " + paymentMethod));
    }
}
