package com.loopers.domain.sharedkernel;
import java.time.LocalDateTime;
import java.util.UUID;

public record KafkaMessage<T>(
        String messageId,       // 메시지 고유 식별자 (중복 방지용)
        String version,         // 스키마 버전
        LocalDateTime publishedAt,
        T payload
) {
    public static <T> KafkaMessage<T> of(T payload) {
        return new KafkaMessage<>(
                UUID.randomUUID().toString(),
                "v1",
                LocalDateTime.now(),
                payload
        );
    }
}
