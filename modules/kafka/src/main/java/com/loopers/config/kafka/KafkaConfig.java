package com.loopers.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String BATCH_LISTENER = "BATCH_LISTENER_DEFAULT";

    private static final int MAX_POLLING_SIZE = 3000;
    private static final int FETCH_MIN_BYTES = (1024 * 1024);
    private static final int FETCH_MAX_WAIT_MS = 5 * 1000;
    private static final int SESSION_TIMEOUT_MS = 60 * 1000;
    private static final int HEARTBEAT_INTERVAL_MS = 20 * 1000;
    private static final int MAX_POLL_INTERVAL_MS = 2 * 60 * 1000;

    /**
     * ProducerFactory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * JSON Converter
     * ByteArrayJsonMessageConverter : Kafka 메시지 바이트(payload)를 Jackson 기반 JSON 직렬화/역직렬화 처리해주는 Converter
     */
    @Bean
    public ByteArrayJsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new ByteArrayJsonMessageConverter(objectMapper);
    }

    /**
     * Batch Listener Container Factory
     * https://tommykim.tistory.com/90 : kafka 성능 개선기 (feat. 배치 리스너)
     * @KafkaListener는 기본적으로 단건 메시지를 받도록 설정돼 있다.
     * 만약 여러 레코드를 배치(batch) 단위로 받고 싶으면, BatchListener 기능을 켜야 한다.
     * 이때 containerFactory로 커스텀 팩토리 빈 이름을 지정해야 List<ConsumerRecord<...>> 형태로 받을 수 있다.
     */
    @Bean(name = BATCH_LISTENER)
    public ConcurrentKafkaListenerContainerFactory<String, Object> defaultBatchListenerContainerFactory(
            KafkaProperties kafkaProperties,
            ByteArrayJsonMessageConverter converter
    ) {
        Map<String, Object> consumerConfig = new HashMap<>(kafkaProperties.buildConsumerProperties());
        consumerConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLLING_SIZE);
        consumerConfig.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, FETCH_MIN_BYTES);
        consumerConfig.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, FETCH_MAX_WAIT_MS);
        consumerConfig.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);
        consumerConfig.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS);
        consumerConfig.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfig));
        // manual ack
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(converter));
        // application에서 몇개의 consumer가 동작하게 끔 할 것인지
        factory.setConcurrency(3);

        // 배치 리스닝 활성화
        factory.setBatchListener(true);

        return factory;
    }

    /**
     * 단건용 기본 리스너 컨테이너 팩토리
     * Bean 이름을 "kafkaListenerContainerFactory" 로 두면 @KafkaListener에서 containerFactory 생략 가능
     */
    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            ByteArrayJsonMessageConverter converter // yml에서 value-deserializer가 ByteArrayDeserializer 이므로 필요
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);

        // ★ 단건 모드
        factory.setBatchListener(false);

        // yml: spring.kafka.listener.ack-mode=manual → 단건이면 보통 즉시 커밋이 안전
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // ByteArray → JSON 역직렬화 (메서드 파라미터 타입으로 바인딩)
        /**
         * ByteArrayJsonMessageConverter는 Spring Kafka의 RecordMessageConverter 구현체예요.
         * 이 컨버터는 KafkaConsumer가 읽어온 byte[] value를 Jackson을 이용해 Java 객체로 역직렬화 해줍니다.
         * 그래서 @KafkaListener 메서드에서 파라미터 타입을 DTO로 선언하면, JSON → DTO 변환이 자동으로 일어나요.
         */
        factory.setRecordMessageConverter(converter);

        // 필요시 조정
        factory.setConcurrency(3);
        return factory;
    }
}
