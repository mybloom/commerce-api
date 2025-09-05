package com.loopers.domain.handle;

import lombok.Builder;

public class EventHandledCommand {
    public record Create(
            String messageId,
            String handler
    ) {}
}
