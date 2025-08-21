package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;
import com.loopers.application.payment.PaymentUseCase;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1ApiController implements PaymentV1ApiSpec {

    private final PaymentUseCase paymentUseCase;

    @PostMapping
    @Override
    public ApiResponse<PaymentV1Response.PaymentResponse> tryPayment(
            @RequestHeader(name = "X-USER-ID", required = true) Long userId,
            @Valid @RequestBody PaymentV1Request.Pay request
    ) {
        log.info("결제 요청 - userId: {}, orderId: {}, paymentMethod: {}",
                userId, request.getOrderId(), request.getPaymentMethod());

        PaymentResult.Pay paymentResult = paymentUseCase.pay(request.convertToCommand());
        PaymentV1Response.PaymentResponse response = PaymentV1Response.PaymentResponse.from(paymentResult);
        log.info("결제 요청 처리 완료 - paymentId: {}, paymentStatus: {}", response.getPaymentId(), response.getPaymentStatus());
        return ApiResponse.success(response);
    }
}
