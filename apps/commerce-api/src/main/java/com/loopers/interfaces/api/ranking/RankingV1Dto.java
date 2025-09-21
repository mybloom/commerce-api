package com.loopers.interfaces.api.ranking;

import com.loopers.application.common.PagingCondition;
import com.loopers.application.ranking.RankingResult;
import com.loopers.support.paging.Pagination;
import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public class RankingV1Dto {

    public record ListViewRequest(
            LocalDate date,  // yyyyMMdd 포맷
            RankingV1Dto.RankingPeriodType periodType, // 일간 / 주간 / 월간
            @Nullable
            PagingCondition pagingCondition
    ) {
        public ListViewRequest {
            if (pagingCondition == null) {
                pagingCondition = new PagingCondition(0, 20);
            }
        }
    }

    public record ListViewResponse(
            RankingV1Dto.RankingPeriodType periodType, // 일간 / 주간 / 월간
            List<RankingItemResponse> rankings,
            PaginationResponse pagination
    ) {
        public static ListViewResponse from(RankingResult.ListView result) {
            List<RankingItemResponse> items = result.items().stream()
                    .map(item -> new RankingItemResponse(
                            item.rank(),
                            item.productId(),
                            item.score(),
                            item.productName(),
                            item.brandName(),
                            item.price(),
                            item.likeCount(),
                            item.status(),
                            item.saleStartDate(),
                            item.createdAt()
                    ))
                    .toList();

            return new ListViewResponse(
                    RankingV1Dto.RankingPeriodType.valueOf(result.periodType().name()),
                    items,
                    PaginationResponse.from(result.pagination()));
        }
    }

    public record RankingItemResponse(
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

    public record PaginationResponse(
            long totalCount,
            int page,
            int size,
            int totalPages,
            boolean hasNext,
            boolean isFirst,
            boolean isLast
    ) {
        public static PaginationResponse from(Pagination pagination) {
            int totalPages = (int) Math.ceil((double) pagination.totalCount() / pagination.size());
            return new PaginationResponse(
                    pagination.totalCount(),
                    pagination.page(),
                    pagination.size(),
                    totalPages,
                    pagination.hasNext(),
                    pagination.page() == 0,
                    pagination.page() >= totalPages - 1
            );
        }
    }

    public enum RankingPeriodType {
        DAILY, WEEKLY, MONTHLY
    }
}
