package com.loopers.domain.sharedkernel;

import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE) // 유틸 클래스 인스턴스 생성 방지
public class KafkaRecordFactory {

    private static final String TYPE_ID_HEADER = "__TypeId__";

    public static <T> ProducerRecord<String, Object> withTypeHeader(
            String topic,
            String key,
            T payload
    ) {
        KafkaMessage<T> message = KafkaMessage.of(payload);
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, message);

        record.headers().remove(TYPE_ID_HEADER);
        record.headers().add(
                TYPE_ID_HEADER,
                KafkaMessage.class.getName().getBytes(StandardCharsets.UTF_8)
        );

        return record;
    }

}
