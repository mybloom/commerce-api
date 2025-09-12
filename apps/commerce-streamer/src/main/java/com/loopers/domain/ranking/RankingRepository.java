package com.loopers.domain.ranking;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.Set;

public interface RankingRepository {

    /**
     * 랭킹 점수 저장
     */
    void saveAll(String key, Set<Ranking> rankings, Duration ttl);

    Set<ZSetOperations.TypedTuple<String>> findTopN(String key, int n);

    Cursor<ZSetOperations.TypedTuple<String>> scan(String key, int count);
}
