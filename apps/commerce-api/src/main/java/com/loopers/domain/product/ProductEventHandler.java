package com.loopers.domain.product;

import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductEventHandler {

    private final ProductService productService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleLikeCountIncrease(LikeEvent.LikeCountIncreased event) {
        log.info("좋아요가 새로 등록되었습니다.productId: {}", event.productId());
        productService.increaseLikeCountAtomically(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleLiDecrease(LikeEvent.LikeCountDecreased event) {
        log.info("좋아요가 해제 되었습니다.productId: {}", event.productId());
        productService.decreaseLikeCount(event.productId());
    }
}
