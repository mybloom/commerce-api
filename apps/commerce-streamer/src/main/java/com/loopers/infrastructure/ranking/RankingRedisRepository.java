package com.loopers.infrastructure.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.loopers.config.redis.RedisConfig.REDIS_TEMPLATE_MASTER;

@RequiredArgsConstructor
@Repository
public class RankingRedisRepository {

    private final @Qualifier(REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> writeTemplate;

    public void zAdd(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        writeTemplate.opsForZSet().add(key, tuples);
    }

    public Long getExpire(String key) {
        return writeTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void expire(String key, Duration duration) {
        writeTemplate.expire(key, duration);
    }

}
