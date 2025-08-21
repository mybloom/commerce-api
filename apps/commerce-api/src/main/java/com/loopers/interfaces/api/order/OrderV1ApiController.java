package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderUseCase;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1ApiController implements OrderV1ApiSpec {

    private final OrderUseCase orderUseCase;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Response.OrderResponse> order(
            @RequestHeader(name = "X-USER-ID", required = true) Long userId,
            @RequestHeader(name = "X-ORDER-REQUEST-KEY", required = true) String orderRequestKey,
            @Valid @RequestBody OrderV1Request.Create request) {

        OrderInfo.Create info = request.convertToOrderInfo(userId, orderRequestKey);
        OrderResult.OrderRequestResult result = orderUseCase.order(info);

        // 로그 출력
        log.info("주문 요청 처리 완료: userId={}, orderRequestKey={}, result={}",
                userId, orderRequestKey, result);
        return ApiResponse.success(OrderV1Response.OrderResponse.from(result));
    }
}
