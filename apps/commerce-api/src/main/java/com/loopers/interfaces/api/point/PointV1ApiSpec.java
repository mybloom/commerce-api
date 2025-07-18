package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Point V1 API", description = "Point management operations")
public interface PointV1ApiSpec {

    @Operation(
            summary = "포인트 충전",
            description = "헤더 X-User-Id 로 식별된 사용자의 포인트를 충전합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "충전 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ApiResponse<PointV1Dto.PointResponse> charge(
            @Parameter(
                    name = "X-USER-ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더)"
            )
            Long userId,
            @Parameter(
                    description = "충전 요청 정보",
                    required = true
            )
            PointV1Dto.ChargeRequest request
    );
}
