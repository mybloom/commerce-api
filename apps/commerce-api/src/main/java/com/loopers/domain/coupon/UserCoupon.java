package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_coupon")
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public void markUsed() {
        this.used = true;
    }

    public void markRestore() {
        this.used = false;
    }

    public void validateUsable() {
        if (this.used) {
            throw new CoreException(ErrorType.CONFLICT, "이미 사용된 쿠폰입니다.");
        }
        coupon.validateUsable();
    }
}
