package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "orders")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    private Money totalAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "payment_amount"))
    private Money paymentAmount;

    private Long paymentId;

    private String orderRequestId; // 멱등키

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderLine> orderLines = new ArrayList<>();

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    public static Order create(Long userId, String orderRequestId) {
        Order order = new Order();
        order.userId = userId;
        order.status = OrderStatus.PENDING;
        order.orderRequestId = orderRequestId;
        order.createdAt = ZonedDateTime.now();
        return order;
    }

    public void addProduct(Long productId, Quantity quantity, Money price) {
        this.orderLines.add(new OrderLine(productId, quantity, price));
    }

    public void calculateTotal() {
        this.totalAmount = orderLines.stream()
            .map(OrderLine::getSubTotal)
            .reduce(Money.ZERO, Money::add);
    }

    public void markPaid(Long paymentId, Money paymentAmount) {
        this.status = OrderStatus.PAID;
        this.paymentId = paymentId;
        this.paymentAmount = paymentAmount;
    }

    public void markFailed() {
        this.status = OrderStatus.FAILED;
    }
}
