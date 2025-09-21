package com.loopers.application.ranking;

import com.loopers.application.common.PagingCondition;
import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingQuery;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.paging.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingUseCase {

    private final RankingService rankingService;
    private final ProductService productService;

    public RankingResult.ListView retrieveRanking(
            final RankingPeriodType rankingPeriodType, final LocalDate date, final PagingCondition pagingCondition
    ) {
        // 1) 랭킹 조회
        Page<RankingQuery.RankingItem> rankingItems = Page.empty(pagingCondition.toPageable());
        if(rankingPeriodType == RankingPeriodType.DAILY) {
            rankingItems = rankingService.retrieveDaily(date, pagingCondition);
        }
        if(rankingPeriodType == RankingPeriodType.WEEKLY) {
            rankingItems = rankingService.retrieveWeekly(date, pagingCondition);
        }
        if(rankingPeriodType == RankingPeriodType.MONTHLY) {
            rankingItems = rankingService.retrieveMonthly(date, pagingCondition);
        }


        if (rankingItems.isEmpty()) {
            return RankingResult.ListView.empty(rankingPeriodType, pagingCondition.page(), pagingCondition.size());
        }

        // 2) productIds 로 상품정보 조회
        List<Long> productIds = rankingItems.stream()
                .map(RankingQuery.RankingItem::productId)
                .toList();
        List<ProductListProjection> productListProjections = productService.getProductsWithBrand(productIds);

        // 3) 매핑 후 결과 반환
        int page = rankingItems.getNumber();
        return RankingResult.ListView.from(
                rankingPeriodType,
                rankingItems.getContent(),
                productListProjections,
                new Pagination(rankingItems.getTotalElements(), page, rankingItems.getSize())
        );
    }
}
