package com.loopers.application.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCommand;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OrderTestHelper {
    private OrderTestHelper() {}

    /**
     * 제품/수량 정보로 orderAmount를 계산하고, discountAmount를 반영해
     * OrderCommand.Complete 를 생성합니다.
     *
     * @param orderId 주문 ID
     * @param products 주문 대상 상품 엔티티들 (price 포함)
     * @param qtyByProductId productId -> quantity
     * @param discountAmount 할인 금액 (없으면 Money.of(0) 전달)
     */
    public static OrderCommand.Complete buildCompleteCommand(
            Long orderId,
            List<Product> products,
            Map<Long, Integer> qtyByProductId,
            Money discountAmount
    ) {
        // 1) ProductCommand.OrderProducts 생성 (productId + quantity)
        ProductCommand.OrderProducts orderProducts = toOrderProducts(qtyByProductId);

        // 2) 주문 총액(orderAmount) 계산: Σ (상품가격 * 수량)
        Money orderAmount = calcOrderAmount(products, qtyByProductId);

        // 3) OrderCommand.Complete 조립
        return OrderCommand.Complete.of(
                orderId,
                products,
                orderProducts,
                orderAmount,
                discountAmount
        );
    }

    /**
     * 할인 없는 기본 케이스(할인 0원) 헬퍼.
     */
    public static OrderCommand.Complete buildCompleteCommand(
            Long orderId,
            List<Product> products,
            Map<Long, Integer> qtyByProductId
    ) {
        return buildCompleteCommand(orderId, products, qtyByProductId, Money.of(0L));
    }

    // -------------------- 내부 헬퍼 --------------------

    private static ProductCommand.OrderProducts toOrderProducts(Map<Long, Integer> qtyByProductId) {
        var orderProductList = qtyByProductId.entrySet().stream()
                .map(e -> ProductCommand.OrderProducts.OrderProduct.of(
                        e.getKey(),
                        e.getValue()
                ))
                .collect(Collectors.toList());

        return ProductCommand.OrderProducts.of(orderProductList);
    }

    private static Money calcOrderAmount(List<Product> products, Map<Long, Integer> qtyByProductId) {
        Money total = Money.of(0L);
        for (Product p : products) {
            int qty = qtyByProductId.getOrDefault(p.getId(), 0);
            if (qty <= 0) continue;

            total = total.add(p.getPrice().multiply(Quantity.of(qty)));
        }
        return total;
    }
}
