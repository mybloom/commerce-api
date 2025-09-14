package com.loopers.application.metrics;

import com.loopers.domain.handle.EventHandled;
import com.loopers.domain.handle.EventHandledCommand;
import com.loopers.domain.handle.EventHandledService;
import com.loopers.domain.metrics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsUseCase {

    private final EventHandledService eventHandledService;
    private final ProductMetricsService productMetricsService;

    @Transactional
    public MetricsQuery.Save save(MetricsCommand.Create command) {
        // 1. 멱등성 확인
        EventHandled eventHandled = eventHandledService.checkIdempotency(
                new EventHandledCommand.Create(command.messageId(), command.handler())
        );

        try {
            // 2. metrics 저장
            return this.process(command, eventHandled.getId());
        } catch (Exception e) {
            log.error("Error saving metrics. msgId={}, handler={}, error={}",
                    command.messageId(), command.handler(), e.getMessage(), e);

            // 실패 처리
            eventHandledService.handleFailure(eventHandled);
            return MetricsQuery.Save.failed(command.messageId(), e.getMessage());
        }
    }

    private MetricsQuery.Save process(MetricsCommand.Create command, Long eventHandledId) {
        // metrics 저장
        productMetricsService.save(command);

        // 성공 처리
        eventHandledService.handleSuccess(eventHandledId);
        return MetricsQuery.Save.created(command.messageId());
    }

    // 구매 이벤트 전용 처리
    @Transactional
    public MetricsQuery.Save savePurchase(MetricsCommand.CreatePurchase command) {
        // 1. 멱등성 확인
        EventHandled eventHandled = eventHandledService.checkIdempotency(
                new EventHandledCommand.Create(command.messageId(), command.handler())
        );

        try {
            // 2. metrics 저장
            return this.processPurchase(command, eventHandled.getId());
        } catch (Exception e) {
            log.error("Error saving metrics. msgId={}, handler={}, error={}",
                    command.messageId(), command.handler(), e.getMessage(), e);

            // 실패 처리
            eventHandledService.handleFailure(eventHandled);
            return MetricsQuery.Save.failed(command.messageId(), e.getMessage());
        }
    }

    private MetricsQuery.Save processPurchase(MetricsCommand.CreatePurchase command, Long eventHandledId) {
        // metrics 저장
        productMetricsService.savePurchase(command);

        // 성공 처리
        eventHandledService.handleSuccess(eventHandledId);
        return MetricsQuery.Save.created(command.messageId());
    }
}
