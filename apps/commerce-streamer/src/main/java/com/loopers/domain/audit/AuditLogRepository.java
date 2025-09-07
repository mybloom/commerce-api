package com.loopers.domain.audit;

import java.util.Optional;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findByMessageIdWithLock(String messageId);
}
