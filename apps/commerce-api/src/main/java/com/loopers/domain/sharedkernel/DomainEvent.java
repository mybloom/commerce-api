package com.loopers.domain.sharedkernel;

import java.time.ZonedDateTime;
import java.util.UUID;

public abstract class DomainEvent {
    private final String eventId;
    private final ZonedDateTime occurredAt;
    private final String name;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = ZonedDateTime.now();
        this.name = getClass().getSimpleName();
    }
}
