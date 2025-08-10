package com.loopers.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_use_history")
public class CouponUseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_coupon_id", nullable = false)
    private Long userCouponId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "used_at", nullable = true)
    private LocalDateTime usedAt;

    @Column(name = "restored_at", nullable = true)
    private LocalDateTime restoredAt;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    public static CouponUseHistory record(Long userCouponId, Long userId, Long orderId, BigDecimal amount) {
        return CouponUseHistory.builder()
                .userCouponId(userCouponId)
                .userId(userId)
                .orderId(orderId)
                .usedAt(LocalDateTime.now())
                .amount(amount)
                .build();
    }

    public static CouponUseHistory restore(Long userCouponId, Long userId, Long orderId, BigDecimal amount) {
        return CouponUseHistory.builder()
                .userCouponId(userCouponId)
                .userId(userId)
                .orderId(orderId)
                .restoredAt(LocalDateTime.now())
                .amount(amount)
                .build();
    }

}
