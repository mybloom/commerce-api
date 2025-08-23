package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderQuery.CreatedOrder createOrderByRequestId(Long userId, String orderRequestId) {
        return orderRepository.findByOrderRequestId(orderRequestId)
                .map(order -> {
                    log.info("기존 주문 발견: orderId={}, orderRequestId={}", order.getId(), orderRequestId);
                    return OrderQuery.CreatedOrder.existing(order);
                })
                .orElseGet(() -> {
                    log.info("새 주문 생성: userId={}, orderRequestId={}", userId, orderRequestId);
                    Order createdOrder = orderRepository.save(Order.create(userId, orderRequestId));
                    return OrderQuery.CreatedOrder.created(createdOrder);
                });
    }

    public Money calculateOrderAmountByAddLines(final Order order, final List<OrderLine> orderLines) {
        order.addOrderLine(orderLines);
        return order.calculateOrderAmount();
    }

    public Money calculatePaymentAmount(Order order, Money discountAmount) {
        order.applyDiscount(discountAmount);
        return order.getPaymentAmount();
    }

    public Order getUserOrderWithLock(Long userId, Long orderId) {
        return orderRepository.findByIdWithOrderLines(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .orElseThrow(() -> new CoreException(ErrorType.FORBIDDEN, "해당 사용자의 주문이 아닙니다."));
    }

    public Order completeOrder(final Order order, final OrderCommand.Complete command) {
        final Money paymentAmount = command.getOrderAmount().subtract(command.getDiscountAmount());
        order.complete(
                command.getOrderLines(),
                command.getOrderAmount(),
                command.getDiscountAmount(),
                paymentAmount
        );

        return orderRepository.save(order);
    }
}
