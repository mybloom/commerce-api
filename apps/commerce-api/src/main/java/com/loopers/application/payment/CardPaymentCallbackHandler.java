package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentCallbackInfo;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CardPaymentCallbackHandler {

    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public void success(PaymentCallbackInfo.ProcessTransaction info){
        Order order = orderService.getUserOrderWithLines(info.orderId());

        //1.재고 차감
        List<OrderLine> orderLines = order.getOrderLines();
        List<ProductCommand.DeductStocks.DeductStock> deductStocks = orderLines.stream()
                .map(orderProduct -> ProductCommand.DeductStocks.DeductStock.of(
                        orderProduct.getProductId(),
                        orderProduct.getQuantity()
                ))
                .collect(Collectors.toList());
        ProductCommand.DeductStocks deductCommand = ProductCommand.DeductStocks.of(deductStocks);
        productService.deductStock(deductCommand);

        //2.결제 완료 처리
        paymentService.completeViaCallback(info.orderId(), info.transactionKey(), info.reason());

        //3.주문 성공 처리
        orderService.success(order.getId());
    }
}
