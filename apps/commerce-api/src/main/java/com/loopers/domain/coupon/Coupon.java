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

    public static Coupon create(String name, DiscountPolicy discountPolicy, LocalDate startAt, LocalDate endAt) {
        if ("".equals(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 이름은 필수입니다.");
        }
        if (discountPolicy == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 정책은 필수입니다.");
        }
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 시작일과 종료일을 입력해야 합니다.");
        }
        return Coupon.builder()
                .name(name)
                .discountPolicy(discountPolicy)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

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
