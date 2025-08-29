package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.sharedkernel.OrderEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderQuery.CreatedOrder createOrderByRequestId(Long userId, String orderRequestId) {
        Order order = orderRepository.findByOrderRequestId(orderRequestId).orElse(null);

        if (order != null) {
            log.info("기존 주문 발견: orderId={}, orderRequestId={}", order.getId(), orderRequestId);
            return OrderQuery.CreatedOrder.existing(order);
        }

        log.info("새 주문 생성: userId={}, orderRequestId={}", userId, orderRequestId);
        Order createdOrder = orderRepository.save(Order.create(userId, orderRequestId));

        //주문 이벤트 발행
        OrderEvent.OrderPending event = new OrderEvent.OrderPending(createdOrder.getId());
        eventPublisher.publishEvent(event);

        return OrderQuery.CreatedOrder.created(createdOrder);
    }


    public Money calculateOrderAmountByAddLines(final Order order, final List<OrderLine> orderLines) {
        order.addOrderLine(orderLines);
        return order.calculateOrderAmount();
    }

    public Money calculatePaymentAmount(Order order, Money discountAmount) {
        order.applyDiscount(discountAmount);
        return order.getPaymentAmount();
    }

    public Order getUserOrderWithLinesByUser(Long userId, Long orderId) {
        return orderRepository.findByIdWithOrderLines(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .orElseThrow(() -> new CoreException(ErrorType.FORBIDDEN, "해당 사용자의 주문이 아닙니다."));
    }

    public Order getUserOrderWithLines(Long orderId) {
        return orderRepository.findByIdWithOrderLines(orderId)
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

    @Transactional
    public void markFailed(final Long orderId) {
        Order order = orderRepository.findByIdWithOrderLines(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문 정보를 찾을 수 없습니다. orderId=" + orderId));
        order.fail();

        //주문 실패 이벤트 발행
        OrderEvent.OrderFailed event = new OrderEvent.OrderFailed(order.getId());
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void success(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문 정보를 찾을 수 없습니다. orderId=" + orderId));
        order.success();

        //주문 성공 이벤트 발행
        OrderEvent.OrderSucceeded event = new OrderEvent.OrderSucceeded(order.getId());
        eventPublisher.publishEvent(event);
    }
}
