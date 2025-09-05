package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Table(name = "orders")
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

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

    @Column(name = "order_request_id", unique = true, nullable = false)
    private String orderRequestId; // 멱등키

    //todo: 관계 설정 블로그에 정리해두기
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();

    public static Order create(Long userId, String orderRequestId) {
        return Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .orderRequestId(orderRequestId)
                .build();
    }

    public void addOrderLine(List<OrderLine> orderLines) {
        if (orderLines.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상품이 없습니다.");
        }
        this.orderLines = orderLines;
    }

    public Money calculateOrderAmount() {
        if (orderLines.isEmpty()) {
            throw new CoreException(ErrorType.CONFLICT, "주문 상품이 없습니다.");
        }

        this.totalAmount = orderLines.stream()
                .map(OrderLine::getSubTotal)
                .reduce(Money.ZERO, Money::add);
        return totalAmount;
    }

    public void applyDiscount(Money discountAmount) {
        this.discountAmount = discountAmount;
        this.paymentAmount = this.totalAmount.subtract(discountAmount);
    }

    public void complete(List<OrderLine> orderLines,
                         Money totalAmount,
                         Money discountAmount,
                         Money paymentAmount) {

        validateDiscountAmount(totalAmount, discountAmount);

        // 주문 상태 변경
        if (this.status != OrderStatus.PENDING) {
            throw new CoreException(ErrorType.CONFLICT, "주문 상태가 올바르지 않습니다.");
        }
        this.status = OrderStatus.COMPLETED;

        // 금액 정보 설정
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paymentAmount = paymentAmount;

        // OrderLine 설정
        //todo: 정리 this.orderLines = orderLines; // OrderLine 리스트를 새로 설정하지 말고, 기존 리스트에 추가하는 방식으로 변경
        this.orderLines.forEach(orderLines::add);

    }

    private void validateDiscountAmount(Money totalAmount, Money discountAmount) {
        if (totalAmount.isLessThan(discountAmount)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액이 주문 금액을 초과할 수 없습니다.");
        }
    }

    public void fail() {
        if (this.status != OrderStatus.COMPLETED) {
            throw new CoreException(ErrorType.CONFLICT, "주문 상태가 올바르지 않습니다.");
        }
        this.status = OrderStatus.FAILED;
    }

    public void success() {
        if (this.status != OrderStatus.COMPLETED) {
            throw new CoreException(ErrorType.CONFLICT, "주문 상태가 올바르지 않습니다.");
        }
        this.status = OrderStatus.SUCCESS;
    }
}
