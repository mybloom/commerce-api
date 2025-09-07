package com.loopers.domain.handle;

import java.util.Optional;

public interface EventHandledRepository {
    Optional<EventHandled> findByMessageIdAndHandlerWithLock(String messageId, String handler);

    Optional<EventHandled> findByIdWithLock(Long eventHandledId);

    EventHandled save(EventHandled eventHandled);
}
