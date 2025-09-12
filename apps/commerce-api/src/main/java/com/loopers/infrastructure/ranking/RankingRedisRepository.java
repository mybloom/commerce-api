package com.loopers.infrastructure.ranking;

import com.loopers.config.redis.RankingKeyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RankingRedisRepository {

    private final RedisTemplate<String, String> readTemplate;

    public Set<ZSetOperations.TypedTuple<String>> findReverseRangeWithScores(String key, long start, long end) {
        return readTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    public long totalCount(String key) {
        Long size = readTemplate.opsForZSet().size(key);
        return size != null ? size : 0L;
    }

}
