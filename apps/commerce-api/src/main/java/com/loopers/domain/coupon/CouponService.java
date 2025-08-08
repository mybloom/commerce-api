package com.loopers.domain.coupon;

import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> findAllValidCouponsOrThrow(List<Long> userCouponIds) {
        if (userCouponIds == null || userCouponIds.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "User coupon IDs must not be null or empty.");
        }

        List<Coupon> coupons = couponRepository.findAllById(userCouponIds);

        Set<Long> foundIds = coupons.stream()
                .map(Coupon::getId)
                .collect(Collectors.toSet());

        if (!foundIds.containsAll(userCouponIds)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰입니다.");
        }

        return coupons;
    }
}
