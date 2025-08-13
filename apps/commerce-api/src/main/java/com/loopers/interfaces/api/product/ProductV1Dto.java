package com.loopers.interfaces.api.product;

import com.loopers.application.common.PagingCondition;
import com.loopers.application.product.ProductQueryResult.ListViewResult;
import com.loopers.domain.product.ProductSortType;
import com.loopers.support.paging.Pagination;
import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class ProductV1Dto {
    public record ListViewRequest(
        @Nullable
        Long brandId,
        @Nullable
        ProductSortType sortCondition,
        @Nullable
        PagingCondition pagingCondition
    ) {
        //todo: 기본조건 설정을 request에서 하는 것이 맞을까? 일단 application layer에서도 해둠.
        public ListViewRequest {
            // 기본 정렬 조건 설정
            if (sortCondition == null) {
                sortCondition = ProductSortType.LATEST;
            }

            // 기본 페이징 조건 설정
            if (pagingCondition == null) {
                pagingCondition = new PagingCondition(0, 10);
            }
        }
    }

    public record DetailViewRequest(
       Long productId
    ){}

    public record ListViewResponse(
        List<ProductListItemResponse> products,
        PaginationResponse paginationResponse
    ) {
        public static ListViewResponse from(ListViewResult result) {
            List<ProductListItemResponse> responseItems = result.products().stream()
                .map(product -> new ProductListItemResponse(
                    product.productId(),
                    product.brandName(),
                    product.productName(),
                    product.price(),
                    product.likeCount(),
                    product.saleStartDate()
                ))
                .toList();

            return new ListViewResponse(
                responseItems,
                PaginationResponse.from(result.pagination())
            );
        }
    }

    public record ProductListItemResponse(
        Long productId,
        String brandName,
        String productName,
        long price,
        int likeCount,
        LocalDate saleStartDate
    ){}

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

    public record DetailViewResponse(
        Long productId,
        String brandName,
        String productName,
        int price,
        int likeCount,
        LocalDateTime createdAt,
        String productStatus,
        String productDescription
    ){}
}
