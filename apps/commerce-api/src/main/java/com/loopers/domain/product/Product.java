package com.loopers.domain.product;

import com.loopers.domain.commonvo.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

import java.time.ZonedDateTime;
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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Convert(converter = MoneyConverter.class)
    private Money price;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Convert(converter = LikeCountConverter.class)
    private LikeCount likeCount;

    private Quantity stockQuantity; //TODO: Inventory에서 관리여부를 고민

    private LocalDate saleStartDate;

    @Column(nullable = false, name = "brand_id")
    private Long brandId;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    public static Product from(
        String name,
        Long price,
        ProductStatus status,
        int likeCount,
        Quantity stockQuantity,
        LocalDate saleStartDate,
        Long brandId
    ) {
        return Product.builder()
            .name(name)
            .price(Money.of(price))
            .status(status)
            .likeCount(LikeCount.from(likeCount))
            .stockQuantity(stockQuantity)
            .saleStartDate(saleStartDate)
            .brandId(brandId)
            .build();
    }

    public static Product testInstance(
        String name,
        Long price,
        ProductStatus status,
        int likeCount,
        Quantity stockQuantity,
        LocalDate saleStartDate,
        Long brandId
    ) {
        return Product.builder()
            .id(0L)
            .name(name)
            .price(Money.of(price))
            .status(status)
            .likeCount(LikeCount.from(likeCount))
            .stockQuantity(stockQuantity)
            .saleStartDate(saleStartDate)
            .brandId(brandId)
            .build();
    }

    public void increaseLikeCount() {
        this.likeCount = likeCount.increase();
    }

    public void decreaseLikeCount() {
        this.likeCount = likeCount.decrease();
    }

    public void deductStock(Quantity quantity) {
        if (this.stockQuantity.isGreaterThanOrEqual(quantity)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        this.stockQuantity = this.stockQuantity.subtract(quantity);
    }

}
