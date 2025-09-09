package com.loopers.domain.handle;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventHandledService {

    private final EventHandledRepository eventHandledRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventHandled checkIdempotency(EventHandledCommand.Create command) {
        try {
            return eventHandledRepository.save(EventHandled.createPending(command.messageId(), command.handler()));
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.CONFLICT, "Duplicate messageId detected: " + command.messageId() + " for handler: " + command.handler());
        }
    }

    @Transactional
    public void handleFailure(EventHandled eventHandled) {
        eventHandled.markFailure();
    }

    @Transactional
    public void handleSuccess(Long eventHandledId) {
        EventHandled eventHandled1 = eventHandledRepository.findByIdWithLock(eventHandledId)
                .orElseThrow(
                        () -> new IllegalStateException("EventHandled not found for eventHandledId: " + eventHandledId)
                );
        eventHandled1.markSuccess();
    }
}
