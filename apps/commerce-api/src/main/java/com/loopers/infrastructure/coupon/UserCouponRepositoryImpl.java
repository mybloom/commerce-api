package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserCouponRepositoryImpl implements UserCouponRepository {
    final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public List<UserCoupon> findAllById(List<Long> userCouponIds) {
        return userCouponJpaRepository.findAllById(userCouponIds);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public Optional<UserCoupon> findById(Long couponId) {
        return userCouponJpaRepository.findById(couponId);
    }
}
