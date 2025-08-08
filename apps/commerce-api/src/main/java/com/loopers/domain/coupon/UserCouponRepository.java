package com.loopers.domain.coupon;

import java.util.List;

public interface UserCouponRepository {
    List<UserCoupon> findAllById(List<Long> userCouponIds);

    UserCoupon save(UserCoupon userCoupon);
}
