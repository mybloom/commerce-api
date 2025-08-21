package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon,Long> {
    List<UserCoupon> findAllByIdInAndUserId(List<Long> userCouponIds, Long userId);
}
