package com.loopers.application.payment;


import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentFailureReason;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCommandOld;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PaymentUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final PaymentService paymentService;
    private final PaymentFailureHandler failureHandler;

    @Transactional
    public PaymentResult.Pay pay(PaymentInfo.Pay payInfo) {
        try {
            return tryPayment(payInfo);
        } catch (CoreException e) {
            failureHandler.handle(payInfo.getUserId(), payInfo, PaymentFailureReason.fromMessage(e.getMessage()));
            throw e;
        }
    }

    @Transactional
    public PaymentResult.Pay tryPayment(final PaymentInfo.Pay payInfo) {
        // 주문 조회
        final Order order = orderService.getUserOrder(payInfo.getUserId(), payInfo.getOrderId());
        final List<OrderLine> orderLines = order.getOrderLines();

        // OrderLine에서 상품 및 수량 확인
        List<Long> productIds = orderLines.stream()
                .map(OrderLine::getProductId)
                .toList();

        // 1. 재고 차감
        List<ProductCommand.OrderProducts.OrderProduct> orderProducts = orderLines.stream()
                .map(orderLine -> ProductCommand.OrderProducts.OrderProduct.of(
                        orderLine.getProductId(),
                        orderLine.getQuantity().getAmount()))
                .toList();
        ProductCommand.OrderProducts command = ProductCommand.OrderProducts.of(orderProducts);
        List<Product> products = productService.validateProductsAndStock(command);

        // Product 매핑을 위한 Map 생성
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<ProductCommandOld.DeductStock> deductStocksCommand = orderLines.stream()
                .map(orderLine -> {
                    Product product = productMap.get(orderLine.getProductId());
                    return ProductCommandOld.DeductStock.of(product, orderLine.getQuantity());
                })
                .collect(Collectors.toList());

        if (!productService.deductStock(deductStocksCommand)) {
            throw new CoreException(ErrorType.CONFLICT, PaymentFailureReason.OUT_OF_STOCK.getMessage());
        }

        // 2. 포인트 결제
        Point point = pointService.findByUserWithLock(payInfo.getUserId());
        pointService.useOrThrow(point, order.getPaymentAmount());

        // 3. 결제 성공 처리
        boolean isOrderConfirm = true;
        Long paymentId = paymentService.saveSuccess(
                payInfo.getOrderId(),
                payInfo.getPaymentMethod(),
                order.getPaymentAmount()
        );

        return PaymentResult.Pay.of(paymentId);
    }
}
