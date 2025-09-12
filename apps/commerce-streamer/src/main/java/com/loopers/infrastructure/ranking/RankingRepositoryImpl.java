package com.loopers.infrastructure.ranking;

import com.loopers.config.redis.RankingKeyUtils;
import com.loopers.domain.ranking.Ranking;
import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingRedisRepository rankingRedisRepository;

    /**
     * 랭킹 저장 시, TTL이 설정되어 있지 않다면 지정된 TTL로 설정
     */
    @Override
    public void saveAll(String key, Set<Ranking> rankings, Duration ttl) {
        Set<ZSetOperations.TypedTuple<String>> tuples = rankings.stream()
                .map(ranking -> ZSetOperations.TypedTuple.of(
                        RankingKeyUtils.generateMemberKey(ranking.productId()), // Redis 전용 포맷
                        ranking.score()
                ))
                .collect(Collectors.toSet());

        rankingRedisRepository.zAdd(key, tuples);

        Long currentTtl = rankingRedisRepository.getExpire(key);
        if (currentTtl == null || currentTtl == -1) {
            rankingRedisRepository.expire(key, ttl);
        }
    }

}
