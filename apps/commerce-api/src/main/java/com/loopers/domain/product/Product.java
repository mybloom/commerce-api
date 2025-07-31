package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.commonvo.LikeCount;
import com.loopers.domain.commonvo.LikeCountConverter;
import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.MoneyConverter;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
@Entity
public class Product extends BaseEntity {

    private String name;

    @Convert(converter = MoneyConverter.class)
    private Money price;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Convert(converter = LikeCountConverter.class)
    private LikeCount likeCount;

    private Long stockQuantity; //TODO: Inventory에서 관리여부를 고민

    private LocalDate saleStartDate;

    @Column(nullable = false, name = "brand_id")
    private Long brandId;

    public static Product from(
        String name,
        Long price,
        ProductStatus status,
        int likeCount,
        Long stockQuantity,
        LocalDate saleStartDate,
        Long brandId
    ) {
        return Product.builder()
            .name(name)
            .price(Money.from(price))
            .status(status)
            .likeCount(LikeCount.from(likeCount))
            .stockQuantity(stockQuantity)
            .saleStartDate(saleStartDate)
            .brandId(brandId)
            .build();
    }

}
