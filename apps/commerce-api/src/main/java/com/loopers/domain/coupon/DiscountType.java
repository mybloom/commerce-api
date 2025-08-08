package com.loopers.domain.coupon;


import java.math.BigDecimal;
import java.math.RoundingMode;
import com.loopers.domain.commonvo.Money;

public enum DiscountType {

    RATE {
        @Override
        public Money calculateDiscountAmount(Long totalPrice, BigDecimal discountValue) {
            if (totalPrice == null || discountValue == null) return Money.ZERO;

            BigDecimal price = BigDecimal.valueOf(totalPrice);
            long discounted = price.multiply(discountValue)
                    .setScale(0, RoundingMode.DOWN)
                    .longValue();

            return Money.of(discounted);
        }
    },

    FIXED_AMOUNT {
        @Override
        public Money calculateDiscountAmount(Long totalPrice, BigDecimal discountValue) {
            if (discountValue == null) return Money.ZERO;

            long discounted = discountValue.setScale(0, RoundingMode.DOWN).longValue();
            return Money.of(discounted);
        }
    };

    public abstract Money calculateDiscountAmount(Long totalPrice, BigDecimal discountValue);
}


