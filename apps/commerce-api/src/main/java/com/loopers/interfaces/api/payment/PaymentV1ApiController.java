package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentProcessResult;
import com.loopers.application.payment.PaymentResult;
import com.loopers.application.payment.PaymentUseCase;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

        var result  = paymentUseCase.pay(request.convertToCommand(userId));
        var outcome = result.outcome();

        if (outcome instanceof PaymentProcessResult.Approved
                || outcome instanceof PaymentProcessResult.Pending) {
            return ApiResponse.success(PaymentV1Response.PaymentResponse.from(result));
        }

        // 실패 : 컨트롤러에서만 CoreException 던지기 (Advice가 409 + ApiResponse.fail 로 변환)
        if (outcome instanceof PaymentProcessResult.Declined declined) {
            throw new CoreException(ErrorType.CONFLICT, declined.reason());
        }

        // 방어적
        throw new CoreException(ErrorType.INTERNAL_ERROR, "UNKNOWN payment OUTCOME");
    }

}
