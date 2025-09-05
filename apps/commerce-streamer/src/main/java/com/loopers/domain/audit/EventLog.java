package com.loopers.domain.audit;


import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;


@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "event_log",
        indexes = {
                @Index(name = "idx_eventlog_topic", columnList = "topic"),
                @Index(name = "idx_eventlog_eventType", columnList = "eventType")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_eventlog_messageId",
                        columnNames = {"messageId"}
                )
        }
)
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트 메시지 고유 ID (멱등성 체크용)
     */
    @Column(nullable = false, length = 64)
    private String messageId;

    /**
     * 이벤트가 발행된 토픽
     */
    @Column(nullable = false, length = 128)
    private String topic;

    /**
     * 이벤트 타입 (예: LikeCountIncreased, OrderCreated 등)
     */
    @Column(nullable = false, length = 64)
    private String eventType;

    /**
     * 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventProcessStatus status;

    /**
     * 원본 Payload (JSON 직렬화)
     */
    @Lob
    @Column(nullable = false)
    private String payload;

    /**
     * 프로듀서가 발행한 시각
     */
    private LocalDateTime publishedAt;

    /**
     * 에러 발생 시 메시지
     */
    @Lob
    private String errorMessage;

    /**
     * DB에 insert된 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 컨슈머가 처리한 시각
     */
    @LastModifiedDate
    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.consumedAt = LocalDateTime.now(); // 컨슈머에서 insert할 때 기본 세팅
    }

    public static EventLog ofSuccess(String messageId,
                                     String topic,
                                     String eventType,
                                     String payload,
                                     LocalDateTime publishedAt) {
        return EventLog.builder()
                .messageId(messageId)
                .topic(topic)
                .eventType(eventType)
                .status(EventProcessStatus.SUCCESS)
                .payload(payload)
                .publishedAt(publishedAt)
                .build();
    }

    public static EventLog ofFailure(String messageId,
                                     String topic,
                                     String eventType,
                                     String payload,
                                     LocalDateTime publishedAt,
                                     String errorMessage) {
        return EventLog.builder()
                .messageId(messageId)
                .topic(topic)
                .eventType(eventType)
                .status(EventProcessStatus.FAILURE)
                .payload(payload)
                .publishedAt(publishedAt)
                .errorMessage(errorMessage)
                .build();
    }

    public void markToSuccess() {
        if(this.status == EventProcessStatus.SUCCESS) {
            log.error(
                    "EventLog is already marked as SUCCESS. id={}, messageId={}",
                    this.id, this.messageId
            );
            throw new IllegalStateException("EventLog is already marked as SUCCESS");
        }
        this.status = EventProcessStatus.SUCCESS;
    }

}
