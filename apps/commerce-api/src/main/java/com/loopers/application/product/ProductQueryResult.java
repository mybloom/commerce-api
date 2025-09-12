package com.loopers.application.product;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.product.ProductQuery;
import com.loopers.support.paging.Pagination;

import java.time.LocalDate;
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
                            p.saleStartDate()
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
            LocalDate saleStartDate
    ) {

    }

    public record CatalogDetailResult(
            Long productId,
            String productName,
            long productPrice,
            int likeCount,
            String productStatus,
            Long brandId,
            String brandName,
            Long rank // 랭킹 정보
    ) {

        //Brand, Product를 받아 CatalogDetailResult 생성하는 정적 메서드
        public static CatalogDetailResult from(Brand brand, ProductQuery.ProductDetailQuery product, Long rank) {
            return new CatalogDetailResult(
                    product.id(),
                    product.name(),
                    product.price().getAmount(),
                    product.likeCount().getValue(),
                    product.status().name(),
                    brand.getId(),
                    brand.getName(),
                    rank
            );
        }
    }
}
