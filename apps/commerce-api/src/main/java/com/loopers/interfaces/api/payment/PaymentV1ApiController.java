package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.dto.PaymentResult;
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

        PaymentResult.Pay result = paymentUseCase.pay(request.convertToCommand(userId));

        return ApiResponse.success(
                PaymentV1Response.PaymentResponse.from(result)
        );
    }

    @PostMapping("/pg/callback")
    @Override
    public void pgCallback(@RequestBody PaymentCallbackDto.ProcessRequest request) {
        log.info("********PG사 콜백 수신");
        log.info("orderId:{}, transactionKey: {}, status: {}, reason: {}",
                request.orderId(), request.transactionKey(), request.status(), request.reason());
        paymentUseCase.pgConclude(request.convertToCommand());
        log.info("********PG사 콜백 처리 완료");
    }
}
