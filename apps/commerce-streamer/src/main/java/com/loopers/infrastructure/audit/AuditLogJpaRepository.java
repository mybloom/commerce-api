package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.AuditLog;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT el FROM AuditLog el WHERE el.messageId = :messageId")
    Optional<AuditLog> findByMessageIdWithLock(@Param("messageId") String messageId);
}
