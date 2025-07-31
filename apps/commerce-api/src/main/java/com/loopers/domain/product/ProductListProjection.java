package com.loopers.domain.product;

import com.loopers.domain.commonvo.LikeCount;
import com.loopers.domain.commonvo.Money;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record ProductListProjection(
    Long productId,
    String productName,
    Money productPrice,
    LikeCount likeCount,
    ProductStatus productStatus,
    LocalDate saleStartDate,
    ZonedDateTime productCreatedAt,
    Long brandId,
    String brandName
) {

}
