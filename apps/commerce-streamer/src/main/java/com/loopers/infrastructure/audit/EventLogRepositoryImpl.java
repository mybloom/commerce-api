package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.EventLog;
import com.loopers.domain.audit.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EventLogRepositoryImpl implements EventLogRepository {
    private final EventLogJpaRepository eventLogJpaRepository;

    @Override
    public EventLog save(EventLog entry) {
        return eventLogJpaRepository.save(entry);
    }
}
