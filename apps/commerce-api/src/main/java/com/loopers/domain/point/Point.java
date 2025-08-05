package com.loopers.domain.point;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "point")
public class Point {
    public static final Money INITIAL_POINT_AMOUNT = Money.ZERO;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false, unique = true)
    private Long userId; //User의 ID를 참조

    private Point(final Long userId) {
        this.userId = userId;
        this.amount = INITIAL_POINT_AMOUNT;
    }

    private Point(final Long userId, final Money amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public static Point createInitial(final Long userId) {
        return new Point(userId);
    }

    public static Point create(final Long userId, final Money amount) {
        validateChargePoint(amount);
        return new Point(userId, amount);
    }

    public void charge(final Money amount) {
        validateChargePoint(amount);
        this.amount = this.amount.add(amount);
    }

    private static void validateChargePoint(Money amount) {
        if (amount == null || amount.isLessThanOne()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 포인트는 1 이상이어야 합니다.");
        }
    }

    public Money balance() {
        return amount;
    }
}
