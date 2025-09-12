package com.loopers.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

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

        // ★ 변경: 프로듀서 직렬화기를 명시하고 __TypeId__ 헤더 자동 첨부 //todo: 아래 내용 정리하기
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true); // __TypeId__ 헤더 ON

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        //todo: 아래 내용 정리하기
        // ★ 변경: ByteArrayDeserializer → JsonDeserializer 로 전환
        // JsonDeserializer 는 __TypeId__ 헤더를 읽어 타입을 결정하고 DTO로 바로 역직렬화
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // ★ 추가: 보안/타입결정 설정
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);                  // 헤더(__TypeId__) 사용
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.loopers.*");             // DTO 패키지 화이트리스트
        props.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);

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
     * Batch Listener Container Factory
     * https://tommykim.tistory.com/90 : kafka 성능 개선기 (feat. 배치 리스너)
     * @KafkaListener는 기본적으로 단건 메시지를 받도록 설정돼 있다.
     * 만약 여러 레코드를 배치(batch) 단위로 받고 싶으면, BatchListener 기능을 켜야 한다.
     * 이때 containerFactory로 커스텀 팩토리 빈 이름을 지정해야 List<ConsumerRecord<...>> 형태로 받을 수 있다.
     */
    @Bean(name = BATCH_LISTENER)
    public ConcurrentKafkaListenerContainerFactory<String, Object> defaultBatchListenerContainerFactory(
            KafkaProperties kafkaProperties,
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler
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
        factory.setConsumerFactory(consumerFactory);
        // manual ack
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler);

        // application에서 몇개의 consumer가 동작하게 끔 할 것인지
        factory.setConcurrency(1);

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
            DefaultErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        // ★ 단건 모드
        factory.setBatchListener(false);

        // manual ack
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // 필요시 조정
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> template) {
        // 실패 레코드를 <원본토픽>.DLT 로 보냄
        var recoverer = new DeadLetterPublishingRecoverer(
                template, (rec, ex) -> new org.apache.kafka.common.TopicPartition(rec.topic() + ".DLT", rec.partition())
        );
        // 0.5초 간격, 2회 재시도(총 3번 시도)
        var backOff = new org.springframework.util.backoff.FixedBackOff(500L, 2L);
        var handler = new org.springframework.kafka.listener.DefaultErrorHandler(recoverer, backOff);

        // 재시도 무의미한 예외는 즉시 DLT
        handler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                org.springframework.messaging.converter.MessageConversionException.class,
                org.springframework.kafka.support.converter.ConversionException.class,
                ClassCastException.class
        );

        return handler;
    }

}
