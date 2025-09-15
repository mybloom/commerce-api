package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentFailureInfo;
import com.loopers.application.payment.dto.PaymentInfo;
import com.loopers.application.payment.dto.PaymentResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.sharedkernel.PaymentEvent;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultPointPaymentProcessor implements PointPaymentProcessor {

    private final PointService pointService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final PaymentFailureHandler failureHandler;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public PaymentResult.Pay process(PaymentInfo.PointPay info, Order order) {
        try {
            return doProcess(info, order);
        } catch (CoreException e) {
            //todo: 실패 이벤트 발행 (롤백 이후에 리스너가 처리) : @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
            //원래도 after rollback 이후에 처리 되는 것이긴하다. 이벤트로 처리했을 때 찾아갈 때 불필요한 에너지가 더 든다고 생각.
            failureHandler.handleFailedPointPayment(
                    PaymentFailureInfo.Fail.of(info, order.getPaymentAmount(), e.getErrorType() + ":" + e.getMessage())
            );
            throw e; //롤백 유도
        }
    }

    public PaymentResult.Pay doProcess(final PaymentInfo.PointPay info, Order order) {
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

        //5. 주문 성공 처리(이벤트)
        PaymentEvent.PaymentCompleted event = new PaymentEvent.PaymentCompleted(payment.getId(), info.getOrderId());
        eventPublisher.publishEvent(event);

        return PaymentResult.Pay.of(payment.getId(), payment.getStatus().name(), payment.getOrderId());
    }
}
