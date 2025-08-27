package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentFailureInfo;
import com.loopers.application.payment.dto.PaymentInfo;
import com.loopers.application.payment.dto.PaymentResult;
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
    private final ProductService productService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final PaymentFailureHandler failureHandler;

    @Transactional
    @Override
    public PaymentResult.Pay process(PaymentInfo.Pay info, Order order) {
        try {
            return doProcess(info, order);
        } catch (CoreException e) {
            failureHandler.handleFailedPointPayment(
                    PaymentFailureInfo.Fail.of(info, order.getPaymentAmount(), e.getErrorType() + ":" + e.getMessage())
            );
            throw e;
        }
    }

    public PaymentResult.Pay doProcess(final PaymentInfo.Pay info, Order order) {
        // 2. 보유 포인트 확인 및 포인트 차감
        Point point = pointService.findByUserWithLock(info.getUserId());
        pointService.use(point, order.getPaymentAmount());

        // 3. 재고 차감
        List<OrderLine> orderLines = order.getOrderLines();
        List<ProductCommand.DeductStocks.DeductStock> deductStocks = orderLines.stream()
                .map(orderProduct -> ProductCommand.DeductStocks.DeductStock.of(
                        orderProduct.getProductId(),
                        orderProduct.getQuantity()
                ))
                .collect(Collectors.toList());
        ProductCommand.DeductStocks deductCommand = ProductCommand.DeductStocks.of(deductStocks);
        productService.deductStock(deductCommand);

        // 4. 결제 완료
        Payment payment = paymentService.createSuccess(info.getUserId(), order.getId(), PaymentMethod.POINT,
                order.getPaymentAmount());

        //5. 주문 성공 처리
        orderService.success(order.getId());

        return PaymentResult.Pay.of(payment.getId(), payment.getStatus().name(), payment.getOrderId());
    }

    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.POINT;
    }
}
