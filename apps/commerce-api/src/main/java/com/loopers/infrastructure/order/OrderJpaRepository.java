package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderRequestId(String orderRequestId);

    @EntityGraph(attributePaths = "orderLines")
    Optional<Order> findByIdWithOrderLines(Long id);
}
