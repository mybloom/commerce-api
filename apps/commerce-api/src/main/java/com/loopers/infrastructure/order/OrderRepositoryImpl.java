package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import java.util.Optional;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findByOrderRequestId(String orderRequestId) {
        return orderJpaRepository.findByOrderRequestId(orderRequestId);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findByIdWithOrderLines(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Long findTotalPaymentAmountByUserId(Long userId) {
        return orderJpaRepository.findTotalPaymentAmountByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "사용자의 결제 금액을 찾을 수 없습니다. userId: " + userId));
    }
}
