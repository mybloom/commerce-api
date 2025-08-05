package com.loopers.domain.order;

import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findByOrderRequestId(String orderRequestId);

    Order save(Order order);

    Optional<Order> findById(Long id);
}
