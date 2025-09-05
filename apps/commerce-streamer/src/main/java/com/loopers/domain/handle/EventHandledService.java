package com.loopers.domain.handle;

import com.loopers.domain.audit.EventLogCommand;
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
    public EventHandled checkIdempotency(EventLogCommand.CreateEventLog command) {
        eventHandledRepository.findByMessageIdWithLock(command.messageId())
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

    public void handleSuccess(EventHandled eventHandled) {
        eventHandled.markSuccess();
    }
}
