package com.loopers.infrastructure.dataplatform;

import com.loopers.domain.product.UserBehaviorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserBehaviorEventListener {

    /**
     * todo: @Transactional이 없어 @TransactionalEventListener는 적용되지 않았음.
     * fallbackExecution = true를 해주면 됨. 또는 @EventListener를 쓰던가.
     */
    @Async("productViewExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handleProductViewEventAfterCommit(UserBehaviorEvent.ProductView event) {
        // 데이터 플랫폼으로 전송
        sendToDataPlatformSystem(event);
    }

    private void sendToDataPlatformSystem(UserBehaviorEvent.ProductView event) {
        log.info("*데이터플랫폼전송:상품조회");
    }
}
