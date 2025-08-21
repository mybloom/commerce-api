package com.loopers.domain.coupon;

import com.loopers.domain.commonvo.Money;

import java.util.List;

public class CouponQuery {
    public record Use(
            List<UserCoupon> userCoupons,
            Money discountAmount
    )
    {}
}
