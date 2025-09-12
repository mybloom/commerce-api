package com.loopers.infrastructure.ranking;

import com.loopers.config.redis.RankingKeyUtils;
import com.loopers.domain.ranking.RankingQuery;
import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingRedisRepository rankingRedisRepository;

    @Override
    public Page<RankingQuery.RankingItem> findAll(LocalDate date, Pageable pageable) {
        // 키 생성
        String key = RankingKeyUtils.generateRankingKey(date);

        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;

        // Redis에서 조회
        Set<ZSetOperations.TypedTuple<String>> tuples =
                rankingRedisRepository.findReverseRangeWithScores(key, start, end);

        // 전체 개수
        long total = rankingRedisRepository.totalCount(key);

        // DTO 변환
        List<RankingQuery.RankingItem> items = new ArrayList<>();
        if (tuples != null) {
            int rank = (int) start + 1;
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String rawValue = tuple.getValue(); // "productId:101"
                Long productId = RankingKeyUtils.parseProductId(rawValue);
                items.add(new RankingQuery.RankingItem(rank++, productId, tuple.getScore()));
            }
        }

        return new PageImpl<>(items, pageable, total);
    }

}
