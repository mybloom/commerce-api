package com.loopers.domain.order;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;

    public Optional<Order> findByOrderRequestId(String orderRequestId) {
        return orderRepository.findByOrderRequestId(orderRequestId);
    }

    public Order createOrder(Long userId, String orderRequestId) {
        Order order = Order.create(userId, orderRequestId);
        return orderRepository.save(order);
    }

    public void markFailed(Order order) {
        order.markFailed();
    }
}
