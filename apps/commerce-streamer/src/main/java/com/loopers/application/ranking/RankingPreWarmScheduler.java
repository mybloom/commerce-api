package com.loopers.application.ranking;

import com.loopers.config.redis.RankingKeyUtils;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingPreWarmScheduler {

    private final RankingService rankingService;
    private static final int TOP_N = 10_000;

    /**
     * 23:50 → TopN 캐리오버
     */
    @Scheduled(cron = "0 50 23 * * *")
    public void carryOverTopN() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        String todayKey = RankingKeyUtils.generateRankingKey(today);
        String tomorrowKey = RankingKeyUtils.generateRankingKey(tomorrow);

        rankingService.carryOverTopN(todayKey, tomorrowKey, TOP_N);
    }

    /**
     * 23:55 → 백그라운드 보강
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void backfillTomorrowKey() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        String todayKey = RankingKeyUtils.generateRankingKey(today);
        String tomorrowKey = RankingKeyUtils.generateRankingKey(tomorrow);

        rankingService.backfillTomorrowKey(todayKey, tomorrowKey);
    }
}
