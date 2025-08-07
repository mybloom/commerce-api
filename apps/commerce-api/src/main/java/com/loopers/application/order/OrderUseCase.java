package com.loopers.application.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.order.*;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;

import java.util.List;
import java.util.stream.Collectors;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderUseCase {

    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;

    private final OrderLineService orderLineService = new OrderLineService();

    @Transactional(noRollbackFor = CoreException.class)
    public OrderResult.OrderRequestResult order(
            final Long userId, String orderRequestId, final List<OrderInfo.ItemInfo> items) {
        // 1. 멱등키 등록 요청(주문 생성 요청) - 기존 주문 존재 할 경우 기존 주문 정보 전달
        final OrderQuery.CreatedOrder resolvedOrderQuery = orderService.createOrderByRequestId(userId, orderRequestId);

        final Order order = resolvedOrderQuery.order();
        if (!resolvedOrderQuery.isNewlyCreated()) {
            return OrderResult.OrderRequestResult.alreadyOrder(order);
        }

        // 2. 상품 유효성 검증
        final List<Product> allValidProducts;
        try {
            allValidProducts = productService.findAllValidProductsOrThrow(
                    items.stream()
                            .map(OrderInfo.ItemInfo::productId)
                            .collect(Collectors.toSet())
            );
        } catch (CoreException e) {
            orderService.failValidation(order);
            throw e;
        }

        // 3. 상품 추가 및 상품 총액 계산
        final List<OrderLine> orderLines = orderLineService.createOrderLines(OrderInfo.toCommands(items), allValidProducts);
        Money orderAmount = orderService.calculateOrderAmountByAddLines(order, orderLines);

        // 4. 할인 정보 확인
        Money discountAmount = Money.ZERO;
        Money paymentAmount = orderService.calculatePaymentAmount(order, discountAmount);

        // 5. 주문 총액만큼 포인트 보유 확인
        try {
            pointService.validateSufficientBalance(userId, paymentAmount);
        } catch (CoreException e) {
            orderService.failValidation(order);
            throw new CoreException(ErrorType.CONFLICT, "잔액이 부족합니다.");
        }

        return OrderResult.OrderRequestResult.from(order);
    }
}
