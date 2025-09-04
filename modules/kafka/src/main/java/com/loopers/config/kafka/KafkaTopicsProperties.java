package com.loopers.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "commerce-kafka.topics")
public class KafkaTopicsProperties {
    private String likeEvent;
}
