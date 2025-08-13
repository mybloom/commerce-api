package com.loopers.testcontainers;


import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisTestContainersConfig {

    private static final RedisContainer redisContainer;

    static {
        redisContainer = new RedisContainer("redis:latest");
        redisContainer.start();
    }

    public RedisTestContainersConfig() {
        System.setProperty("datasource.redis.database", "0");
        System.setProperty("datasource.redis.master.host", redisContainer.getHost());
        System.setProperty("datasource.redis.master.port", String.valueOf(redisContainer.getFirstMappedPort())); // master.port 수정
        System.setProperty("datasource.redis.replicas[0].host", redisContainer.getHost());
        System.setProperty("datasource.redis.replicas[0].port", String.valueOf(redisContainer.getFirstMappedPort()));
    }
}
