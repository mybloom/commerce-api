package com.loopers.infrastructure.ranking;

import com.loopers.config.redis.RankingKeyUtils;
import com.loopers.domain.ranking.MonthlyRankingMv;
import com.loopers.domain.ranking.RankingQuery;
import com.loopers.domain.ranking.RankingRepository;
import com.loopers.domain.ranking.WeeklyRankingMv;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository{

    private final RankingRedisRepository rankingRedisRepository;
    private final WeeklyRankingMvJpaRepository weeklyRankingMvJpaRepository;
    private final MonthlyRankingMvJpaRepository monthlyRankingMvJpaRepository;

    @Override
    public Page<RankingQuery.RankingItem> findDailyAll(LocalDate date, Pageable pageable) {
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

    @Override
    public Long findDailyRankByDateAndProductId(LocalDate date, Long productId) {
        return rankingRedisRepository.findDailyRankByDateAndProductId(date, productId);
    }

    @Override
    public Page<RankingQuery.RankingItem> findWeeklyAll(LocalDate date, Pageable pageable) {
        Page<WeeklyRankingMv> page = weeklyRankingMvJpaRepository
                .allByAggDateOrderByScoreDescProductIdAsc(date, pageable);

        // Page 번호와 페이지 크기를 이용해서 rank 시작값 계산
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        List<RankingQuery.RankingItem> rankingItems = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(startRank);

        for (WeeklyRankingMv entity : page.getContent()) {
            rankingItems.add(new RankingQuery.RankingItem(
                    counter.getAndIncrement(),       // rank
                    entity.getProductId(),           // productId
                    entity.getScore()               // score
            ));
        }

        return new PageImpl<>(rankingItems, pageable, page.getTotalElements());
    }

    @Override
    public Page<RankingQuery.RankingItem> findMonthlyAll(LocalDate date, Pageable pageable) {
        Page<MonthlyRankingMv> page = monthlyRankingMvJpaRepository.allByAggDateOrderByScoreDescProductIdAsc(date, pageable);

        // Page 번호와 페이지 크기를 이용해서 rank 시작값 계산
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        List<RankingQuery.RankingItem> rankingItems = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(startRank);

        for (MonthlyRankingMv entity : page.getContent()) {
            rankingItems.add(new RankingQuery.RankingItem(
                    counter.getAndIncrement(),       // rank
                    entity.getProductId(),           // productId
                    entity.getScore()               // score
            ));
        }

        return new PageImpl<>(rankingItems, pageable, page.getTotalElements());
    }
}
