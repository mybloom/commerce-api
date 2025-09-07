package com.loopers.domain.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_log",
        indexes = {
                @Index(name = "idx_time", columnList = "created_at"),
                @Index(name = "idx_msg", columnList = "message_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /** Surrogate PK (AUTO_INCREMENT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 메시지 단위 키 */
    @Column(name = "message_id", length = 64, nullable = false)
    private String messageId;

    /**
     * 이벤트 타입 (예: LikeCountIncreased, OrderCreated 등)
     */
    @Column(nullable = false, length = 64)
    private String eventType;


    /** Kafka 토픽명 */
    @Column(name = "topic", length = 128, nullable = false)
    private String topic;

    /** Kafka 파티션 번호 */
    @Column(name = "partition_no", nullable = false)
    private Integer partitionNo;

    /** Kafka 오프셋 */
    @Column(name = "offset_no", nullable = false)
    private Long offsetNo;

    /** Kafka 메시지 키 */
    @Column(name = "key_value", length = 512)
    private String keyValue;

    /** 로그를 남긴 핸들러/컨슈머 식별 */
    @Column(name = "handler", length = 64)
    private String handler;

    /** Kafka 페이로드 원본 (JSON 문자열 저장) */
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;


    //todo: 현재는 문자열 eventType 으로 대체
//    /** 처리 결과 상태 */
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 16)
//    private Result result;

    /** 에러 요약문 */
    @Column(name = "error_message")
    private String errorMessage;

    /** 로그 적재 시각 */
    @Column(name = "created_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public enum Result {
        RECEIVED,
        SUCCESS,
        FAILED
    }

    // ----------------------------
    // 정적 팩토리 메서드
    // ----------------------------

    public static AuditLog create(
            String messageId,
            String eventType,
            String topic,
            Integer partitionNo,
            Long offsetNo,
            String keyValue,
            String handler,
            String payload
    ) {
        return AuditLog.builder()
                .messageId(messageId)
                .eventType(eventType)
                .topic(topic)
                .partitionNo(partitionNo)
                .offsetNo(offsetNo)
                .keyValue(keyValue)
                .handler(handler)
                .payload(payload)
                .build();
    }

}
