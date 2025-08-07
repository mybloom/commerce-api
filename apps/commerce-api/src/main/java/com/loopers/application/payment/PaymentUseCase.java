package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PaymentUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final PaymentService paymentService;

    public PaymentResult.Pay pay(Long userId, PaymentInfo.Pay payInfo) {
        // 0. 주문 조회 및 수량 확인
        Order order = orderService.getUserOrder(userId, payInfo.orderId());
        List<OrderLine> orderLines = order.getOrderLines();

        List<ProductCommand.CheckStock> checkStocksCommand = orderLines.stream()
                .map(orderLine -> ProductCommand.CheckStock.of(orderLine.getProductId(), orderLine.getQuantity()))
                .collect(Collectors.toList());

        // 1. 재고 차감
        if (!productService.deductStock(checkStocksCommand)) {
            return fail(order, payInfo, "재고가 부족합니다.");
        }

        // 2. 포인트 결제
        if (!pointService.use(userId, order.getPaymentAmount())) {
            return fail(order, payInfo, "포인트 결제에 실패했습니다.");
        }

        // 3. 결제 성공 처리
        boolean isPaymentConfirmed = true;
        orderService.finalizePaymentResult(order, isPaymentConfirmed);
        Long paymentId = paymentService.save(payInfo.orderId(), payInfo.paymentMethod(), order.getPaymentAmount(), isPaymentConfirmed);

        return PaymentResult.Pay.of(paymentId, isPaymentConfirmed);
    }

    // 공통 실패 처리 메서드
    private PaymentResult.Pay fail(Order order, PaymentInfo.Pay payInfo, String errorMessage) {
        boolean isPaymentConfirmed = false;

        orderService.finalizePaymentResult(order, isPaymentConfirmed);
        paymentService.save(payInfo.orderId(), payInfo.paymentMethod(), order.getPaymentAmount(), isPaymentConfirmed);

        throw new CoreException(ErrorType.CONFLICT, errorMessage);
    }

}
