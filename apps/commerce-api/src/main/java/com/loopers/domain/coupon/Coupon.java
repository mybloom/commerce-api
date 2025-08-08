package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    private DiscountPolicy discountPolicy;

    @Column(nullable = false)
    private LocalDate startAt;

    @Column(nullable = false)
    private LocalDate endAt;

    private boolean isUsable() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startAt)
                && !today.isAfter(endAt);
    }

    public void validateUsable() {
        if (!isUsable()) {
            throw new CoreException(ErrorType.CONFLICT, "사용할 수 없는 쿠폰입니다.");
        }
    }
}
