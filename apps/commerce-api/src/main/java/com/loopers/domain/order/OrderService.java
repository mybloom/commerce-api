package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderQuery.CreatedOrder createOrderByRequestId(Long userId, String orderRequestId) {
        return orderRepository.findByOrderRequestId(orderRequestId)
                .map(OrderQuery.CreatedOrder::existing)
                .orElseGet(() -> {
                    Order createdOrder = orderRepository.save(Order.create(userId, orderRequestId));
                    return OrderQuery.CreatedOrder.created(createdOrder);
                });
    }

    public void failValidation(final Order order) {
        order.failValidation();
    }

    public Money calculateOrderAmountByAddLines(final Order order, final List<OrderLine> orderLines) {
        order.addOrderLine(orderLines);
        return order.calculateOrderAmount();
    }

    public Money calculatePaymentAmount(Order order, Money discountAmount) {
        order.applyDiscount(discountAmount);
        return order.getPaymentAmount();
    }

    public Order getUserOrder(Long userId, Long orderId) {
        return orderRepository.findByIdWithOrderLines(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .orElseThrow(() -> new CoreException(ErrorType.FORBIDDEN, "해당 사용자의 주문이 아닙니다."));
    }

    public void finalizeOrderResult(Order order, boolean isPaymentConfirmed) {
        if (isPaymentConfirmed) {
            order.markPaid();
        } else {
            order.failPaid();
        }
    }
}
