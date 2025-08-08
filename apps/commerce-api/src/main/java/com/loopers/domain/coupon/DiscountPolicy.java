package com.loopers.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class DiscountPolicy {

    @Convert(converter = DiscountTypeConverter.class)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(precision = 8, scale = 3, name = "discount_rate", nullable = false)
    private BigDecimal discountValue; // 예: 10% => 0.100, 10,000 => 10000.000

    public static DiscountPolicy of(
            DiscountType discountType,
            BigDecimal discountValue
    ) {
        return DiscountPolicy.builder()
                .discountType(discountType)
                .discountValue(discountValue)
                .build();
    }
}
