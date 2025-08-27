package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payment V1 API", description = "Payment management operations")
public interface PaymentV1ApiSpec {

    @Operation(
            summary = "결제 요청",
            description = "헤더 X-USER-ID로 식별된 사용자의 주문에 대한 결제를 시도합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 시도 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문/사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "결제 불가능한 상태")
    })
    ApiResponse<PaymentV1Response.PaymentResponse> tryPayment(
            @Parameter(
                    name = "X-USER-ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더)"
            )
            Long userId,
            @RequestBody(description = "결제 정보")
            PaymentV1Request.Pay request
    );

    @Operation(
            summary = "PG사 결제 콜백 수신",
            description = "PG사로부터 결제 결과 콜백을 수신합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "콜백 정상 처리"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    void pgCallback(
            @RequestBody(description = "PG사 결제 콜백 정보")
            PaymentCallbackDto.ProcessRequest request
    );
}
