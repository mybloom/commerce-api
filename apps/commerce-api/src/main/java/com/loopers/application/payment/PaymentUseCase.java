package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
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
        //0-1 orderId로 orderLines 가져오기. : 주문 수량 확인
        //0-2 orderId로 paymentAmount 가져오기 : 결제 요청
        Order order = orderService.getUserOrder(userId, payInfo.orderId());
        List<OrderLine> orderLines = order.getOrderLines();

        //stream 탐색
        List<ProductCommand.CheckStock> checkStocksCommand = orderLines.stream()
                .map(orderLine ->
                        ProductCommand.CheckStock.of(orderLine.getProductId(), orderLine.getQuantity())
                )
                .collect(Collectors.toList());

        //1. 재고 차감 요청
        //todo: 재고 부족 시 상품 상태 일시품절 처리 (비동기로 변경)
        productService.deductStock(checkStocksCommand);

        //2. 포인트 결제 요청(포인트 차감)

        //3. 결제 성공시, 결제 정보 저장

        return null;
    }
}
