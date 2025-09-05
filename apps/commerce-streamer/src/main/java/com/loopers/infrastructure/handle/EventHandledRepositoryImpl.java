package com.loopers.infrastructure.handle;

import com.loopers.domain.handle.EventHandled;
import com.loopers.domain.handle.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class EventHandledRepositoryImpl implements EventHandledRepository {
    private final EventHandledJpaRepository eventHandledJpaRepository;

    @Override
    public Optional<EventHandled> findByMessageIdAndHandlerWithLock(String messageId, String handler) {
        return eventHandledJpaRepository.findByMessageIdAndHandlerWithLock(messageId, handler);
    }

    @Override
    public Optional<EventHandled> findByIdWithLock(Long eventHandledId) {
        return eventHandledJpaRepository.findByIdWithLock(eventHandledId);
    }

    @Override
    public EventHandled save(EventHandled eventHandled) {
        return eventHandledJpaRepository.save(eventHandled);
    }
}
