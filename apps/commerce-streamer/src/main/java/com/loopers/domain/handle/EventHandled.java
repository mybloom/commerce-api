package com.loopers.domain.handle;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "event_handled",
        uniqueConstraints = {
                // message_id + handler(컨슈머 그룹ID) 조합의 유니크 제약조건 : 동일 메시지를 동일 컨슈머 그룹이 중복 처리하지 않도록 방지
                @UniqueConstraint(name = "uk_message_handler", columnNames = {"message_id", "handler"})
        },
        indexes = {
                @Index(name = "idx_handler_status", columnList = "handler, status"),
                @Index(name = "idx_next_retry", columnList = "next_retry_at")
        }
)
public class EventHandled {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", length = 64, nullable = false)
    private String messageId;

    @Column(name = "handler", length = 64, nullable = false)
    private String handler; //컨슈머 그룹ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventHandledStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSuccess() {
        if (this.status == EventHandledStatus.SUCCESS) {
            return true;
        }
        return false;
    }

    public static EventHandled createPending(String messageId, String handler) {
        return EventHandled.builder()
                .messageId(messageId)
                .handler(handler)
                .status(EventHandledStatus.PENDING)
                .attemptCount(1)
                .build();
    }

    public void markSuccess() {
        if(this.status != EventHandledStatus.PENDING) {
            throw new IllegalStateException("Only PENDING events can be marked as SUCCESS. Current status: " + this.status);
        }
        this.status = EventHandledStatus.SUCCESS;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailure() {
        if(this.status != EventHandledStatus.PENDING) {
            throw new IllegalStateException("Only PENDING events can be marked as FAILURE. Current status: " + this.status);
        }
        this.status = EventHandledStatus.FAILED;
        this.attemptCount += 1;
        this.lastError = "Processing failed at " + LocalDateTime.now(); // 실제 에러 메시지로 교체 필요
    }
}
