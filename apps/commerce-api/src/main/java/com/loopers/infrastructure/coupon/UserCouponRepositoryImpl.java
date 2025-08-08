package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserCouponRepositoryImpl implements UserCouponRepository {
    final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public List<UserCoupon> findAllById(List<Long> userCouponIds) {
        return userCouponJpaRepository.findAllById(userCouponIds);
    }
}
