package com.loopers.application.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderUseCase {

    private final OrderService orderService;
    private final ProductService productService;

    @Transactional
    public OrderResult.OrderRequestResult placeOrder(Long userId, String orderRequestId, List<OrderInfo.ItemInfo> items) {
        // 1. 멱등성 검사 및 기존 주문 존재 여부 확인
        Optional<Order> existing = orderService.findByOrderRequestId(orderRequestId);
        if (!existing.isEmpty()) {
            return OrderResult.OrderRequestResult.alreadyOrder(existing.get());
        }

        // 2. 새 주문 생성 및 저장
        Order order = orderService.createOrder(userId, orderRequestId);

        // 3. 상품 유효성 검증
        Set<Long> productIds = items.stream()
            .map(OrderInfo.ItemInfo::productId)
            .collect(Collectors.toSet());

        List<Product> products = productService.getProducts(productIds.stream().toList());
        if (products.size() != productIds.size()) {
            orderService.markFailed(order);
            throw new CoreException(ErrorType.NOT_FOUND, "상품 정보가 유효하지 않습니다");
        }

        // 4. 상품 추가 및 총액 계산
        for (OrderInfo.ItemInfo item : items) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(item.productId()))
                .findFirst()
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));

            order.addProduct(product.getId(), Quantity.of(item.quantity()), Money.of(item.price()));
        }
        order.calculateTotal();

        return OrderResult.OrderRequestResult.newlyCreated(order);
    }
}
