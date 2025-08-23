package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "payment")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "payment_amount", nullable = false))
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "failure_reason", nullable = true)
    private PaymentFailureReason failureReason;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;


    private Payment(Long orderId, PaymentMethod paymentMethod, Money amount, PaymentStatus paymentStatus) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.createdAt = ZonedDateTime.now();
    }

    private Payment(Long orderId, PaymentMethod paymentMethod, PaymentStatus paymentStatus, PaymentFailureReason failureReason) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.failureReason = failureReason;
        this.createdAt = ZonedDateTime.now();
    }

    public static Payment createInit(Long orderId, PaymentMethod paymentMethod, Money amount) {
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
        }

        return new Payment(orderId, paymentMethod, amount, PaymentStatus.PENDING);
    }

    public void success() {
        if(this.paymentStatus != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태가 PENDING이 아닙니다. 현재 상태: " + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.CONFIRMED;
    }

    public void fail() {
        if(this.paymentStatus != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태가 PENDING이 아닙니다. 현재 상태: " + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }
}
