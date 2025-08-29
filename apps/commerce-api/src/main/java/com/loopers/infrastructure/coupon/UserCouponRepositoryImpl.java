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
    public List<UserCoupon> findAllByIdInAndUserId(List<Long> userCouponIds, Long userId) {
        return userCouponJpaRepository.findAllByIdInAndUserId(userCouponIds, userId);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public Optional<UserCoupon> findById(Long couponId) {
        return userCouponJpaRepository.findById(couponId);
    }

    @Override
    public List<UserCoupon> findAllByOrderIdAndUserId(Long orderId, Long userId) {
        return userCouponJpaRepository.findAllByOrderIdAndUserId(orderId, userId);
    }

    @Override
    public List<UserCoupon> findAllByOrderId(Long orderId) {
        return userCouponJpaRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<UserCoupon> findAllByIdInAndUserIdWithLock(List<Long> userCouponIds, Long userId) {
        return userCouponJpaRepository.findAllByIdInAndUserIdWithLock(userCouponIds, userId);
    }

}
