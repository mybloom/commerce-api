package com.loopers.domain.handle;

import java.util.Optional;

public interface EventHandledRepository {
    Optional<EventHandled> findByMessageIdWithLock(String messageId);

    EventHandled save(EventHandled eventHandled);
}
