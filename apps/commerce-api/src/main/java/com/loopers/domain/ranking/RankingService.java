package com.loopers.domain.ranking;

import com.loopers.application.common.PagingCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public Page<RankingQuery.RankingItem> retrieveDaily(LocalDate date, PagingCondition pagingCondition) {
        Pageable pageable = pagingCondition.toPageable();
        Page<RankingQuery.RankingItem> rankingItems = rankingRepository.findDailyAll(date, pageable);

        return rankingItems;
    }

    public Long retrieveRankByDateAndProductId(final LocalDate date, final Long productId) {
        return rankingRepository.findDailyRankByDateAndProductId(date, productId);
    }

    public Page<RankingQuery.RankingItem> retrieveWeekly(final LocalDate date, final PagingCondition pagingCondition) {
        final Page<RankingQuery.RankingItem> weeklyAll = rankingRepository.findWeeklyAll(date, pagingCondition.toPageable());
        if (!weeklyAll.isEmpty()) {
            return weeklyAll;
        }

        return rankingRepository.findWeeklyAll(date.minusDays(1), pagingCondition.toPageable());
    }

    public Page<RankingQuery.RankingItem> retrieveMonthly(final LocalDate date, final PagingCondition pagingCondition) {
        final Page<RankingQuery.RankingItem> monthlyAll = rankingRepository.findMonthlyAll(date, pagingCondition.toPageable());
        if (!monthlyAll.isEmpty()) {
            return monthlyAll;
        }

        return rankingRepository.findMonthlyAll(date.minusDays(1), pagingCondition.toPageable());
    }
}
