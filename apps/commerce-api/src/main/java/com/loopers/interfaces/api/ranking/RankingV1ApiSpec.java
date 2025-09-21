package com.loopers.interfaces.api.ranking;

import com.loopers.application.common.PagingCondition;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.ProductV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Ranking V1 API", description = "Product ranking operations")
public interface RankingV1ApiSpec {

    @Operation(
            summary = "상품 랭킹 조회",
            description = "특정 날짜 기준으로 상품 랭킹을 조회합니다. " +
                    "랭킹은 인기 지표(좋아요, 조회수, 판매량 등)에 기반하며, " +
                    "조회 결과는 페이징 및 크기(size) 조건을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 랭킹 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ApiResponse<RankingV1Dto.ListViewResponse> retrieveRanking(
            @Parameter(
                    name = "periodType",
                    required = true,
                    in = ParameterIn.QUERY,
                    description = "랭킹 구분 (일간, 주간, 월간)"
            )
            @RequestParam RankingV1Dto.RankingPeriodType periodType,
            @Parameter(
                    name = "date",
                    required = true,
                    in = ParameterIn.QUERY,
                    description = "랭킹 기준 일자 (yyyyMMdd)"
            )
            @DateTimeFormat(pattern = "yyyyMMdd")
            LocalDate date,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "랭킹 목록 페이지 조건 (pagingCondition 포함)"
            )
            PagingCondition pagingCondition
    );
}
