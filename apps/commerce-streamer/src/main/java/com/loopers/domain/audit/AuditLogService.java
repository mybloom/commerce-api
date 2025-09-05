package com.loopers.domain.audit;

import com.loopers.domain.handle.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final EventHandledRepository eventHandledRepository;

    public AuditLog save(final AuditLogCommand.Create command) {
        AuditLog auditLog = AuditLog.create(
                command.messageId(),
                command.eventType(),
                command.topic(),
                command.partitionNo(),
                command.offsetNo(),
                command.keyValue(),
                command.handler(),
                command.payload()
        );
        return auditLogRepository.save(auditLog);
    }
}
