package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon,Long> {
    List<UserCoupon> findAllByIdInAndUserId(List<Long> userCouponIds, Long userId);

    List<UserCoupon> findAllByOrderIdAndUserId(Long orderId, Long userId);

    List<UserCoupon> findAllByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.id IN :userCouponIds AND uc.userId = :userId")
    List<UserCoupon> findAllByIdInAndUserIdWithLock(List<Long> userCouponIds, Long userId);
}
