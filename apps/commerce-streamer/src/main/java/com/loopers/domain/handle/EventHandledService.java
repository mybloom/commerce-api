package com.loopers.domain.handle;

import com.loopers.domain.audit.AuditLogCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        eventHandledRepository.findByMessageIdAndHandlerWithLock(command.messageId(), command.handler())
                .ifPresent(existing -> {
                    if (existing.isSuccess()) {
                        throw new IllegalStateException("Duplicate messageId detected with SUCCESS status: " + command.messageId());
                    }else{
                        log.info("Duplicate messageId detected but status is not SUCCESS. Continuing processing: {}", command.messageId());
                    }
                });

        return eventHandledRepository.save(EventHandled.createPending(command.messageId(), command.handler()));
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
