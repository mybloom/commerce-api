package com.loopers.domain.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    public EventLog saveLog(EventLog eventLog) {
        return eventLogRepository.save(eventLog);
    }
}
