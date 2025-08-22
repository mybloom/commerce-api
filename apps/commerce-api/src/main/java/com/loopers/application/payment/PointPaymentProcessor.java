package com.loopers.application.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class PointPaymentProcessor implements PaymentProcessor {

    private final PointService pointService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final PaymentFailureHandler paymentFailureHandler;


//    @Transactional
    @Override
    public PaymentProcessResult process(final PaymentInfo.Pay info, Payment payment, Money amount) {
        // 포인트 결제 정보
        final PaymentInfo.PointPay pointPayInfo = (PaymentInfo.PointPay) info;

        // 1. 결제할 포인트 금액 확인
        Order order = orderService.getUserOrderWithLock(info.getUserId(), info.getOrderId());
        List<OrderLine> orderLines = order.getOrderLines();

        try {
            // 2. 보유 포인트 확인 및 포인트 차감
            Point point = pointService.findByUserWithLock(info.getUserId());
            pointService.useOrThrow(point, amount);

            // 3. 재고 차감
            List<ProductCommand.DeductStocks.DeductStock> deductStocks = orderLines.stream()
                    .map(orderProduct -> ProductCommand.DeductStocks.DeductStock.of(
                            orderProduct.getProductId(),
                            orderProduct.getQuantity()
                    ))
                    .collect(Collectors.toList());
            ProductCommand.DeductStocks deductCommand = ProductCommand.DeductStocks.of(deductStocks);
            productService.deductStock(deductCommand);
        } catch (CoreException e) {
            return new PaymentProcessResult.Declined(e.getMessage());
        }

        // 4. 결제 완료
        paymentService.success(order.getId(), PaymentMethod.POINT, amount);

        return new PaymentProcessResult.Approved(payment.getId().toString());
    }

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.POINT;
    }
}
