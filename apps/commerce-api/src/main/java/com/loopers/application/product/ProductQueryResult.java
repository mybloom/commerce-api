package com.loopers.application.product;


import com.loopers.domain.product.ProductListProjection;
import com.loopers.support.paging.Pagination;
import java.time.ZonedDateTime;
import java.util.List;

public class ProductQueryResult {

    public record ListViewResult(
        List<ListViewItemResult> products,
        Pagination pagination
    ) {

        public static ListViewResult from(List<ProductListProjection> projections, Pagination pagination) {
            List<ListViewItemResult> items = projections.stream()
                .map(p -> new ListViewItemResult(
                    p.productId(),
                    p.brandId(),
                    p.brandName(),
                    p.productName(),
                    p.productPrice().getAmount(),
                    p.likeCount().getValue(),
                    p.productCreatedAt()
                ))
                .toList();

            return new ListViewResult(items, pagination);
        }
    }

    public record ListViewItemResult(
        Long productId,
        Long brandId,
        String brandName,
        String productName,
        long price,
        int likeCount,
        ZonedDateTime createdAt
    ) {

    }
}
