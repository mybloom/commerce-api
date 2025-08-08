package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderRequestId(String orderRequestId);

    @EntityGraph(attributePaths = "orderLines")
    Optional<Order> findById(Long id);

    @Query("SELECT SUM(o.paymentAmount.amount) FROM Order o WHERE o.userId = :userId")
    Optional<Long> findTotalPaymentAmountByUserId(@Param("userId") Long userId);

}
