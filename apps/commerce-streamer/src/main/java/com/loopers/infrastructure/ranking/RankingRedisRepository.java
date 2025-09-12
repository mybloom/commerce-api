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

    /**
     * 특정 키에서 Top N 멤버와 점수를 조회
     *
     * @param key Redis ZSET key (예: rank:all:product:20250911)
     * @param n   가져올 개수
     * @return Top N 결과 (멤버, 점수 포함)
     */
    public Set<ZSetOperations.TypedTuple<String>> findTopN(String key, long n) {
        return writeTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);
    }

    /**
     * ZSCAN - Redis ZSET 비블로킹 순회
     *
     * @param key   ZSET 키
     * @param count 한 번에 가져올 최대 개수 (힌트 값)
     * @return Cursor for iteration
     */
    public Cursor<ZSetOperations.TypedTuple<String>> scan(String key, int count) {
        ScanOptions options = ScanOptions.scanOptions()
                .count(count)
                .build();
        return writeTemplate.opsForZSet().scan(key, options);
    }
}
