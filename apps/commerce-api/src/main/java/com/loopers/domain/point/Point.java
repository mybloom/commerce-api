package com.loopers.domain.point;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.payment.PaymentFailureReason;
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

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance", nullable = false))
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

    public void use(Money paymentAmount) {
        if( paymentAmount == null || paymentAmount.isLessThan(Money.ZERO)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트는 0 이상이어야 합니다.");
        }
        if (this.amount.isLessThan(paymentAmount)) {
            throw new CoreException(ErrorType.CONFLICT, PaymentFailureReason.INSUFFICIENT_BALANCE.getMessage());
        }
        this.amount = this.amount.subtract(paymentAmount);
    }
}
