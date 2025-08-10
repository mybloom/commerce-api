package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {
    List<UserCoupon> findAllById(List<Long> userCouponIds);

    UserCoupon save(UserCoupon userCoupon);

    Optional<UserCoupon> findById(Long couponId);
}
