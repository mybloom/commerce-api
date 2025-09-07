package com.loopers.interfaces.consumer;

import com.loopers.config.kafka.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoKafkaConsumer {

    @KafkaListener(
            topics = {"${demo-kafka.test.topic-name}"},
            //Spring Kafka에서는 @KafkaListener가 실행될 때, 메시지를 실제로 가져와 실행하는 ConcurrentKafkaListenerContainerFactory라는 객체가 필요
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void demoListener(
            //배치 메시지를 처리하기 위해 List<ConsumerRecord<Object, Object>> 타입으로 설정
            List<ConsumerRecord<Object, Object>> messages,
            Acknowledgment acknowledgment
    ) {
        System.out.println(messages);
        acknowledgment.acknowledge(); // manual ack
    }
}
