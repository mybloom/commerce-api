package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Table(name = "orders")
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount"))
    private Money totalAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "discount_amount"))
    private Money discountAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "payment_amount"))
    private Money paymentAmount;

    private Long paymentId;

    private String orderRequestId; // 멱등키

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    public static Order create(Long userId, String orderRequestId) {
        return Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .orderRequestId(orderRequestId)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void addOrderLine(List<OrderLine> orderLines) {
        if(orderLines.isEmpty()){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상품이 없습니다.");
        }
        this.orderLines = orderLines;
    }

    public Money calculateOrderAmount() {
        if(orderLines.isEmpty()){
            throw new CoreException(ErrorType.CONFLICT, "주문 상품이 없습니다.");
        }

        this.totalAmount = orderLines.stream()
                .map(OrderLine::getSubTotal)
                .reduce(Money.ZERO, Money::add);
        return totalAmount;
    }

    public void failValidation() {
        this.status = OrderStatus.VALIDATION_FAILED;
    }

    public void markPaid(Long paymentId, Money paymentAmount) {
        this.status = OrderStatus.PAID;
        this.paymentId = paymentId;
        this.paymentAmount = paymentAmount;
    }

    public void markFailed() {
        this.status = OrderStatus.PAID_FAILED;
    }

    public void applyDiscount(Money discountAmount) {
        this.discountAmount = discountAmount;
        this.paymentAmount = this.totalAmount.subtract(discountAmount);
    }
}
