package com.loopers.domain.sharedkernel;

import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE) // 유틸 클래스 인스턴스 생성 방지
public class KafkaRecordFactory {

    private static final String TYPE_ID_HEADER = "__TypeId__";


    public static <T> ProducerRecord<String, Object> withTypeHeader(
            String topic, String key, T payload) {

        KafkaMessage<T> message = KafkaMessage.of(payload);
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, payload);

        // payload 타입을 헤더에 넣어줌 (Deserializer가 보고 역직렬화)
        record.headers().remove(TYPE_ID_HEADER);
        record.headers().add(TYPE_ID_HEADER, payload.getClass().getName().getBytes(StandardCharsets.UTF_8));

        // KafkaMessage 메타데이터를 헤더로 따로 추가
        record.headers().add("messageId", message.messageId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("version", message.version().getBytes(StandardCharsets.UTF_8));
        record.headers().add("publishedAt", message.publishedAt().toString().getBytes(StandardCharsets.UTF_8));

        return record;
    }
}
