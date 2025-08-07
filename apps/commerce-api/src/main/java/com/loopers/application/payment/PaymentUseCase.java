package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentFailureReason;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PaymentUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final PaymentService paymentService;

    @Transactional(noRollbackFor = CoreException.class)
    public PaymentResult.Pay pay(Long userId, PaymentInfo.Pay payInfo) {
        // 0. 주문 조회 및 수량 확인
        Order order = orderService.getUserOrder(userId, payInfo.orderId());
        List<OrderLine> orderLines = order.getOrderLines();

        List<ProductCommand.CheckStock> checkStocksCommand = orderLines.stream()
                .map(orderLine ->
                        ProductCommand.CheckStock.of(orderLine.getProductId(), orderLine.getQuantity()))
                .collect(Collectors.toList()
                );

        // 1. 재고 차감
        if (!productService.deductStock(checkStocksCommand)) {
            throwPaymentFailure(order, payInfo, PaymentFailureReason.OUT_OF_STOCK);
        }

        // 2. 포인트 결제
        if (!pointService.use(userId, order.getPaymentAmount())) {
            throwPaymentFailure(order, payInfo, PaymentFailureReason.INSUFFICIENT_BALANCE);
        }

        // 3. 결제 성공 처리
        boolean isOrderConfirm = true;
        orderService.finalizeOrderResult(order, isOrderConfirm);
        Long paymentId = paymentService.saveSuccess(payInfo.orderId(), payInfo.paymentMethod(), order.getPaymentAmount());

        return PaymentResult.Pay.of(paymentId);
    }

    // 공통 실패 처리 메서드
    private PaymentResult.Pay throwPaymentFailure(Order order, PaymentInfo.Pay payInfo, PaymentFailureReason failureReason) {
        boolean isOrderConfirm = false;
        orderService.finalizeOrderResult(order, isOrderConfirm);

        paymentService.saveFailure(payInfo.orderId(), payInfo.paymentMethod(), order.getPaymentAmount(), failureReason);

        throw new CoreException(ErrorType.CONFLICT, failureReason.getMessage());
    }

}
