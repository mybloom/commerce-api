package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
@Entity
public class Payment extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false, updatable = false, unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "payment_amount", nullable = false))
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status;

    @Column(name = "failure_reason", nullable = true)
//    private PaymentFailureReason failureReason;
    private String failureReason;

    private Payment(Long userId, Long orderId, PaymentMethod paymentMethod, Money amount, PaymentStatus status) {
        this.userId = userId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }

    private Payment(Long userId, Long orderId, PaymentMethod paymentMethod, Money amount, PaymentStatus status,
                    String failureReason) {
        this.userId = userId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
    }

    public static Payment createInit(Long userId, Long orderId, PaymentMethod paymentMethod, Money amount) {
        validateCreate(orderId, paymentMethod, amount);
        return new Payment(userId, orderId, paymentMethod, amount, PaymentStatus.PENDING);
    }

    public static Payment createFail(Long userId, Long orderId, PaymentMethod paymentMethod, Money amount, String failureReason) {
        validateCreate(orderId, paymentMethod, amount);
        return new Payment(userId, orderId, paymentMethod, amount, PaymentStatus.FAILED, failureReason);
    }

    public static Payment createSuccess(Long userId, Long orderId, PaymentMethod paymentMethod, Money amount) {
        validateCreate(orderId, paymentMethod, amount);
        return new Payment(userId, orderId, paymentMethod, amount, PaymentStatus.SUCCESS);
    }

    public void success() {
        if(this.status != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태가 PENDING이 아닙니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.SUCCESS;
    }

    public void fail(String failureReason) {
        if(this.status != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태가 PENDING이 아닙니다. 현재 상태: " + this.status);
        }

        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    private static void validateCreate(Long orderId, PaymentMethod paymentMethod, Money amount) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 주문 ID입니다: " + orderId);
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 수단은 필수 값입니다.");
        }
        if (amount == null || amount.isLessThan(Money.ZERO)) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
    }
}
