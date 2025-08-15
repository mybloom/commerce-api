package com.loopers.domain.product;

import com.loopers.domain.commonvo.LikeCount;
import com.loopers.domain.commonvo.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ProductQuery {
    public record ProductDetailQuery(
            Long id,
            String name,
            Money price,
            LikeCount likeCount,
            ProductStatus status,
            LocalDate saleStartDate,
            ZonedDateTime createdAt,
            Long brandId
    ) {
        public static ProductQuery.ProductDetailQuery from(Product product) {
            if (product == null) return null;
            return new ProductQuery.ProductDetailQuery(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getLikeCount(),
                    product.getStatus(),
                    product.getSaleStartDate(),
                    product.getCreatedAt(),
                    product.getBrandId()
            );
        }
    }
}
