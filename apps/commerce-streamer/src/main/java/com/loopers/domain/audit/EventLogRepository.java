package com.loopers.domain.audit;

public interface EventLogRepository {

    EventLog save(EventLog entry);
}
