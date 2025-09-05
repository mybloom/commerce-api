package com.loopers.application.audit;


import com.loopers.domain.audit.AuditLogQuery;
import com.loopers.domain.audit.AuditLogService;
import com.loopers.domain.audit.EventLogCommand;
import com.loopers.domain.handle.EventHandled;
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
    public AuditLogQuery.Save save(EventLogCommand.CreateEventLog command) {
        // 1. 멱등성 확인
        EventHandled eventHandled = eventHandledService.checkIdempotency(command);

        try {
            // 2. auditLog 저장
            return this.process(command, eventHandled);
        } catch (Exception e) {
            // 실패 처리
            eventHandledService.handleFailure(eventHandled);
            return AuditLogQuery.Save.duplicated(e.getMessage());
        }
    }

    private AuditLogQuery.Save process(EventLogCommand.CreateEventLog command, EventHandled eventHandled) {
        // auditLog 저장
        auditLogService.save(command);

        // 성공 처리
        eventHandledService.handleSuccess(eventHandled);
        return AuditLogQuery.Save.created(command.messageId());
    }

}
