package com.loopers.application.audit;


import com.loopers.domain.audit.AuditLogQuery;
import com.loopers.domain.audit.AuditLogService;
import com.loopers.domain.audit.AuditLogCommand;
import com.loopers.domain.handle.EventHandled;
import com.loopers.domain.handle.EventHandledCommand;
import com.loopers.domain.handle.EventHandledService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AuditUseCase {

    private final EventHandledService eventHandledService;
    private final AuditLogService auditLogService;

    @Transactional
    public AuditLogQuery.Save save(AuditLogCommand.Create command) {
        // 1. 멱등성 확인
        EventHandled createdEventHandled = eventHandledService.checkIdempotency(
                new EventHandledCommand.Create(command.messageId(), command.handler())
        );

        try {
            // 2. auditLog 저장
            return this.process(command, createdEventHandled.getId());
        } catch (Exception e) {
            // 실패 처리
            eventHandledService.handleFailure(createdEventHandled);
            return AuditLogQuery.Save.duplicated(e.getMessage());
        }
    }

    private AuditLogQuery.Save process(AuditLogCommand.Create command, Long eventHandledId) {
        // auditLog 저장
        auditLogService.save(command);

        // 성공 처리
        eventHandledService.handleSuccess(eventHandledId);
        return AuditLogQuery.Save.created(command.messageId());
    }

}
