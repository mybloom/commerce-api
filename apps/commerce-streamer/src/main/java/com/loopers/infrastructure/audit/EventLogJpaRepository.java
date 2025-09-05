package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogJpaRepository extends JpaRepository<EventLog, Long> {
}
