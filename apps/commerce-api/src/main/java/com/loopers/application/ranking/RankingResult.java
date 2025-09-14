package com.loopers.application.ranking;

import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.ranking.RankingQuery;
import com.loopers.support.paging.Pagination;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankingResult {

    public record ListView(
            List<ListItem> items,
            Pagination pagination
    ) {
        public static ListView from(List<RankingQuery.RankingItem> rankingItems,
                                    List<ProductListProjection> projections,
                                    Pagination pagination) {

            // productId → projection 매핑
            Map<Long, ProductListProjection> projectionMap = projections.stream()
                    .collect(Collectors.toMap(ProductListProjection::productId, p -> p));

            List<ListItem> items = rankingItems.stream()
                    .map(ranking -> {
                        ProductListProjection projection = projectionMap.get(ranking.productId());
                        return new ListItem(
                                ranking.rank(),
                                ranking.productId(),
                                ranking.score(),
                                projection != null ? projection.productName() : null,
                                projection != null ? projection.brandName() : null,
                                projection != null ? projection.productPrice().getAmount() : 0L,
                                projection != null ? projection.likeCount().getValue() : 0,
                                projection != null ? projection.productStatus().name() : null,
                                projection != null ? projection.saleStartDate() : null,
                                projection != null ? projection.productCreatedAt() : null
                        );
                    })
                    .toList();

            return new ListView(items, pagination);
        }

        public static ListView empty(int page, int size) {
            return new ListView(
                    List.of(),
                    new Pagination(page, size, 0)
            );
        }
    }

    public record ListItem(
            int rank,
            Long productId,
            double score,
            String productName,
            String brandName,
            long price,
            int likeCount,
            String status,
            LocalDate saleStartDate,
            ZonedDateTime createdAt
    ) {
    }
}
