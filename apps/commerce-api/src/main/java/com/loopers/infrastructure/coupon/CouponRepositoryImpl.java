package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CouponRepositoryImpl implements CouponRepository {
    final CouponJpaRepository couponJpaRepository;

    @Override
    public List<Coupon> findAllById(List<Long> userCouponIds) {
        return couponJpaRepository.findAllById(userCouponIds);
    }
}
