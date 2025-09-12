package com.loopers.config.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ranking")
public class RankingProperties {

    private Weights weights;
    private long ttl; // 초 단위
    private double carryOverWeight;

    @Getter
    @Setter
    public static class Weights {
        private double like;
        private double order;
        private double view;
    }

    // Duration 으로 꺼낼 수 있는 커스텀 getter
    public Duration getTtlDuration() {
        return Duration.ofSeconds(ttl);
    }
}
