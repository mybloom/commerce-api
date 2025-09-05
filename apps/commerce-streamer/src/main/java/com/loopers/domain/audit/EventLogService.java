package com.loopers.domain.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventLogService {

    private final EventLogRepository eventLogRepository;


    @Transactional
    public EventLogQuery.Save saveLog(final EventLogCommand.CreateEventLog command) {
        // 중복 처리 확인 - messageId, status 기준
        Optional<EventLog> existing = eventLogRepository.findByMessageIdWithLock(command.messageId());
        if (existing.isPresent()) {
            EventLog existingLog = existing.get();
            if (existingLog.getStatus() == EventProcessStatus.SUCCESS) {
                log.info("Duplicate messageId detected but status is SUCCESS. Skipping processing. messageId={}",
                        command.messageId());
                return EventLogQuery.Save.duplicate(existingLog);
            }

            log.warn("Duplicate messageId detected: {}. Existing log will be overwritten.", command.messageId());
        }

        // 저장
        eventLogRepository.save(EventLog.ofSuccess(
                        command.messageId(),
                        command.topic(),
                        command.eventType(),
                        command.payload(),
                        command.publishedAt()
                )
        );
        return EventLogQuery.Save.created(command.messageId());
    }
}
