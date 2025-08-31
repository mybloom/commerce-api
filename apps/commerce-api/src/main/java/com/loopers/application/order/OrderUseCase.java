package com.loopers.application.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;

import java.util.List;

import com.loopers.domain.sharedkernel.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderUseCase {

    private final OrderService orderService;
    private final ProductService productService;
            private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResult.OrderRequestResult order(OrderInfo.Create orderInfo) {
        // 1. 멱등키 등록 요청(주문 생성 요청) - 기존 주문 존재 할 경우 기존 주문 정보 전달
        final OrderQuery.CreatedOrder createdOrder = orderService.createOrderByRequestId(orderInfo.getUserId(), orderInfo.getOrderRequestKey());

        final Order order = createdOrder.order();
        if (!createdOrder.isNewlyCreated()) {
            return OrderResult.OrderRequestResult.alreadyOrder(order);
        }

        final ProductCommand.OrderProducts productCommand = orderInfo.convertToProductCommand();
        // 2. 상품 유효성 및 재고 검증 //todo: 재고 예약 //todo: 여기에 왜 비관적락 걸었는지 적어두기(지금은 재고 검증만 한다)
        List<Product> products = productService.validateProductsAndStock(productCommand);

        //상품 총 금액 계산
        final Money orderAmount = productService.calculateTotalAmount(products, productCommand);
        // 3. 쿠폰 유효성 및 할인 금액 계산
        final Money discountAmount = couponService.calculateDiscountAmount(
                orderInfo.convertToCouponCommand(orderAmount.getAmount(), order.getId())
        );

        // 4. 주문 확정
        final OrderCommand.Complete completeCommand = OrderCommand.Complete.of(
                order.getId(),
                products,
                productCommand,
                orderAmount,
                discountAmount
        );
        final Order completedOrder = orderService.completeOrder(order, completeCommand);

        // 주문 완료 이벤트 발행
        OrderEvent.OrderCompleted event = new OrderEvent.OrderCompleted(
                completedOrder.getId(),
                orderInfo.getUserId(),
                orderAmount,
                orderInfo.getUserCouponIds()
        );
        eventPublisher.publishEvent(event);

        return OrderResult.OrderRequestResult.completedOrder(completedOrder);
    }

}
