package com.loopers.infrastructure.handle;

import com.loopers.domain.handle.EventHandled;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EventHandled e WHERE e.messageId = :messageId AND e.handler = :handler")
    Optional<EventHandled> findByMessageIdAndHandlerWithLock(@Param("messageId") String messageId,
                                                             @Param("handler") String handler);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EventHandled e WHERE e.id = :eventHandledId")
    Optional<EventHandled> findByIdWithLock(@Param("eventHandledId") Long eventHandledId);
}
