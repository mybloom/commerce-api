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

    /**
     * 특정 상품의 순위 조회 (0 = 1위): +1 해서 API에선 "1위, 2위..."로 표현
     */
    public Long findRankByDateAndProductId(LocalDate date, Long productId) {
        String key = RankingKeyUtils.generateRankingKey(date);
        String member = RankingKeyUtils.generateMemberKey(productId);

        Long rank = readTemplate.opsForZSet().reverseRank(key, member);
        return rank != null ? rank + 1 : null;
    }
}
