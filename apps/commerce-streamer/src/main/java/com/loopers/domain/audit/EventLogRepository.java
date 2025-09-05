package com.loopers.domain.audit;

import java.util.Optional;

public interface EventLogRepository {

    EventLog save(EventLog entry);

    Optional<EventLog> findByMessageIdWithLock(String messageId);
}
