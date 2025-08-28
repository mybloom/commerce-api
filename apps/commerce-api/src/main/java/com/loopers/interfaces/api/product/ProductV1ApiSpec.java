package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product V1 API", description = "Product catalog management operations")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품 목록 조회",
            description = "브랜드, 정렬 조건, 페이징 조건을 포함하여 상품 목록을 조회합니다. " +
                    "로그인하지 않아도 조회 가능하며, 로그인 사용자는 X-USER-ID 헤더를 통해 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ApiResponse<ProductV1Dto.ListViewResponse> retrieveListView(
            @Parameter(
                    name = "X-USER-ID",
                    required = false,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더, 선택값)"
            )
            Long userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "상품 목록 조회 조건 (brandId, sortCondition, pagingCondition 포함)"
            )
            ProductV1Dto.ListViewRequest request
    );

    @Operation(
            summary = "상품 상세 조회",
            description = "특정 상품의 상세 정보를 조회합니다. " +
                    "로그인하지 않아도 조회 가능하며, 로그인 사용자는 X-USER-ID 헤더를 통해 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 없음")
    })
    ApiResponse<ProductV1Dto.DetailViewResponse> retrieveDetailView(
            @Parameter(
                    name = "X-USER-ID",
                    required = false,
                    in = ParameterIn.HEADER,
                    description = "사용자 식별자 (헤더, 선택값)"
            )
            Long userId,
            @Parameter(
                    name = "productId",
                    required = true,
                    in = ParameterIn.PATH,
                    description = "상품 식별자"
            )
            Long productId
    );
}
