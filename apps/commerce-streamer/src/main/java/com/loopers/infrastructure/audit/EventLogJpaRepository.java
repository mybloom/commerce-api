package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.EventLog;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventLogJpaRepository extends JpaRepository<EventLog, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT el FROM EventLog el WHERE el.messageId = :messageId")
    Optional<EventLog> findByMessageIdWithLock(@Param("messageId") String messageId);
}
