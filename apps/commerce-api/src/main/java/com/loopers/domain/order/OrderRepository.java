package com.loopers.domain.order;

import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findByOrderRequestId(String orderRequestId);

    Order save(Order order);

    Optional<Order> findByIdWithOrderLines(Long id);

    Long findTotalPaymentAmountByUserId(Long userId);

    Optional<Order> findById(Long orderId);
}
