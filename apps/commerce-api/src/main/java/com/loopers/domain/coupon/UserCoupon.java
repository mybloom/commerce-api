package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Version
    private Long version;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static UserCoupon create(Long userId, Coupon coupon) {
        if (userId == null || coupon == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 아이디와 쿠폰은 필수입니다.");
        }
        return UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .used(false)
                .issuedAt(LocalDate.now())
                .build();
    }

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
