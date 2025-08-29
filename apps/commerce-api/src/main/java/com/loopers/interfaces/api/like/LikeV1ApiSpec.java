package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Like V1 API", description = "Like management operations")
public interface LikeV1ApiSpec {

    @Operation(
            summary = "상품 좋아요 등록",
            description = "헤더 X-USER-ID 로 식별된 사용자가 특정 상품에 대해 좋아요를 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 사용자 없음")
    })
    ApiResponse<LikeV1Dto.RegisterResponse> register(
            @Parameter(
                    name = "X-USER-ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더)"
            )
            Long userId,
            @Parameter(
                    name = "productId",
                    required = true,
                    in = ParameterIn.PATH,
                    description = "좋아요를 등록할 상품 식별자"
            )
            Long productId
    );

    @Operation(
            summary = "상품 좋아요 취소",
            description = "헤더 X-USER-ID 로 식별된 사용자가 특정 상품에 대해 등록한 좋아요를 취소합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 사용자 없음")
    })
    ApiResponse<LikeV1Dto.RemoveResponse> remove(
            @Parameter(
                    name = "X-USER-ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더)"
            )
            Long userId,
            @Parameter(
                    name = "productId",
                    required = true,
                    in = ParameterIn.PATH,
                    description = "좋아요를 취소할 상품 식별자"
            )
            Long productId
    );
}
