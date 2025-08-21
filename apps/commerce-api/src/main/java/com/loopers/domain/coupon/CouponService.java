package com.loopers.domain.coupon;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponService {

    private final UserCouponRepository userCouponRepository;

    public Money applyCouponDiscount(final CouponCommand.ApplyDiscount command) {
        // 쿠폰이 없으면 할인 금액 0 반환
        final List<Long> userCouponIds = command.getUserCouponIds();
        if (userCouponIds == null || command.getUserCouponIds().isEmpty()) {
            return Money.ZERO;
        }

        // 1. 사용자 쿠폰 조회
        List<UserCoupon> userCoupons = userCouponRepository.findAllByIdInAndUserId(userCouponIds, command.getUserId());

        // 2. 요청한 쿠폰 수와 조회된 쿠폰 수 일치 확인
        if (userCoupons.size() != userCouponIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰이 포함되어 있습니다.");
        }

        Money orderAmount = command.getOrderAmount();
        Money totalDiscount = userCoupons.stream()
                .peek(UserCoupon::validateUsable) // 3. 검증
                .map(userCoupon -> userCoupon.calculateDiscount(orderAmount))  // 4. 할인 계산
                .reduce(Money.ZERO, Money::add);

        if (orderAmount.isLessThan(totalDiscount)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액이 주문 금액을 초과할 수 없습니다.");
        }

        // 5. 쿠폰 사용 처리
        userCoupons.forEach(UserCoupon::markUsed);

        return totalDiscount;
    }
}
