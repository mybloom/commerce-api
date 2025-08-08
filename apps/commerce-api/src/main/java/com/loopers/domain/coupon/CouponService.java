package com.loopers.domain.coupon;

import com.loopers.domain.commonvo.Money;
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

    private final UserCouponRepository userCouponRepository;

    public List<UserCoupon> findAllValidCouponsOrThrow(List<Long> userCouponIds) {
        if (userCouponIds == null || userCouponIds.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "User coupon IDs must not be null or empty.");
        }

        List<UserCoupon> userCoupons = userCouponRepository.findAllById(userCouponIds);

        Set<Long> foundIds = userCoupons.stream()
                .map(UserCoupon::getId)
                .collect(Collectors.toSet());

        if (!foundIds.containsAll(userCouponIds)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰입니다.");
        }

        return userCoupons;
    }

    public Money use(List<UserCoupon> userCoupons,Money orderAmount) {
        if (userCoupons == null || userCoupons.isEmpty()) {
            return Money.ZERO;
        }

        // 유효성 검사
        userCoupons.forEach(UserCoupon::validateUsable);

        // 할인 금액 계산 후 합산
        Money totalDiscount = userCoupons.stream()
                .map(userCoupon -> {
                    Coupon coupon = userCoupon.getCoupon();
                    return coupon.getDiscountPolicy()
                            .getDiscountType()
                            .calculateDiscountAmount(orderAmount.getAmount(), coupon.getDiscountPolicy().getDiscountValue());
                })
                .reduce(Money.ZERO, Money::add);

        return totalDiscount;
    }


}
