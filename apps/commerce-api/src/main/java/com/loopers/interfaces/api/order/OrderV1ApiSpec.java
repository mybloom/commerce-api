package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Order V1 API", description = "Order management operations")
public interface OrderV1ApiSpec {

    @Operation(
            summary = "주문 요청",
            description = "헤더 X-USER-ID 로 식별된 사용자의 주문을 생성합니다. " +
                    "X-ORDER-REQUEST-KEY를 전달하면 동일 키에 대한 중복 요청은 서버에서 멱등 처리됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품/사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 부족 또는 유효하지 않은 상태")
    })
    ApiResponse<OrderV1Response.OrderResponse> order(
            @Parameter(
                    name = "X-USER-ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더)"
            )
            Long userId,
            @Parameter(
                    name = "X-ORDER-REQUEST-KEY",
                    required = false,
                    in = ParameterIn.HEADER,
                    description = "멱등성 키(중복 요청 방지)"
            )
            String idempotencyKey,
            @RequestBody(description = "주문 정보")
            OrderV1Request.Create request
    );
}
