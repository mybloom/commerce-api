package com.loopers.infrastructure.http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "pgClient",
        url = "http://localhost:8082",
        configuration = FeignClientTimeoutConfig.class
)
public interface PgFeignClient {

    @PostMapping("/api/v1/payments")
    PgClientDto.PgAuthResponse requestPayment(@RequestHeader("X-USER-ID") String userId, @RequestBody PgClientDto.PgAuthRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgClientDto.PgTxDetailResponse getTransaction(@RequestHeader("X-USER-ID") String userId, @PathVariable("transactionKey") String transactionKey);

    @GetMapping("/api/v1/payments")
    PgClientDto.PgTxListResponse getPaymentsByOrderId(@RequestHeader("X-USER-ID") String userId, @RequestParam("orderId") String orderId);

}
