package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderQuery.ResolvedOrderQuery resolveOrderByRequestId(Long userId, String orderRequestId) {
        return orderRepository.findByOrderRequestId(orderRequestId)
                .map(OrderQuery.ResolvedOrderQuery::existing)
                .orElseGet(() -> {
                    Order createdOrder = orderRepository.save(Order.create(userId, orderRequestId));
                    return OrderQuery.ResolvedOrderQuery.created(createdOrder);
                });
    }

    public Order createOrder(Long userId, String orderRequestId) {
        Order order = Order.create(userId, orderRequestId);
        return orderRepository.save(order);
    }

    public void failValidation(final Order order) {
        order.failValidation();
    }

    public void markFailed(Order order) {
        order.markFailed();
    }

    public Money calculateOrderAmountByAddLines(final Order order, final List<OrderLine> orderLines) {
        order.addOrderLine(orderLines);
        return order.calculateOrderAmount();
    }

    public Money calculatePaymentAmount(Order order) {
        order.applyDiscount(Money.ZERO);
        return order.getPaymentAmount();
    }
}
