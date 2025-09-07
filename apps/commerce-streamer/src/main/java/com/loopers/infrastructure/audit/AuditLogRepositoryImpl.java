package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.AuditLog;
import com.loopers.domain.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public AuditLog save(AuditLog entry) {
        return auditLogJpaRepository.save(entry);
    }

    @Override
    public Optional<AuditLog> findByMessageIdWithLock(String messageId) {
        return auditLogJpaRepository.findByMessageIdWithLock(messageId);
    }
}
