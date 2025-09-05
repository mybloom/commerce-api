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
    public Optional<EventHandled> findByMessageIdWithLock(String messageId) {
        return eventHandledJpaRepository.findByMessageIdWithLock(messageId);
    }

    @Override
    public EventHandled save(EventHandled eventHandled) {
        return eventHandledJpaRepository.save(eventHandled);
    }
}
