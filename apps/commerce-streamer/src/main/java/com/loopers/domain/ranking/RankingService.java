package com.loopers.domain.ranking;

import com.loopers.config.redis.RankingKeyUtils;
import com.loopers.config.redis.RankingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingRepository rankingRepository;
    private final RankingProperties rankingProperties;

    public void rank(List<RankingCommand.ProductMetrics> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        Map<LocalDate, List<RankingCommand.ProductMetrics>> groupedByDate =
                commands.stream().collect(Collectors.groupingBy(RankingCommand.ProductMetrics::metricsDate));

        groupedByDate.forEach((date, metricsList) -> {
            // 키 생성
            String dateKey = RankingKeyUtils.generateRankingKey(date);

            // 점수 계산 후 도메인 객체 변환
            Set<Ranking> rankings = metricsList.stream()
                    .map(productMetric -> new Ranking(
                            productMetric.productId(),
                            calculateScore(productMetric)
                    ))
                    .collect(Collectors.toSet());

            // TTL 적용하여 저장
            rankingRepository.saveAll(dateKey, rankings, rankingProperties.getTtlDuration());
        });
    }

    private double calculateScore(RankingCommand.ProductMetrics metric) {
        return metric.likeCount().doubleValue() * rankingProperties.getWeights().getLike()
                + metric.purchaseCount().doubleValue() * rankingProperties.getWeights().getOrder()
                + metric.viewCount().doubleValue() * rankingProperties.getWeights().getView();
    }

    /**
     * TopN 캐리오버 (오늘 → 내일)
     */
    public void carryOverTopN(String todayKey, String tomorrowKey, int n) {
        // 오늘 키에서 TopN 조회 (Redis 전용 타입)
        Set<ZSetOperations.TypedTuple<String>> topN = rankingRepository.findTopN(todayKey, n);

        if (topN == null || topN.isEmpty()) {
            log.warn("오늘 키에 데이터 없음, 캐리오버 생략: todayKey={}", todayKey);
            return;
        }

        // 가중치 적용한 score 내일 키에 저장
        Set<Ranking> rankings = topN.stream()
                .filter(tuple -> tuple.getValue() != null && tuple.getScore() != null)
                .map(tuple -> new Ranking(
                        RankingKeyUtils.parseProductId(tuple.getValue()),  // "productId:123" → 123
                        tuple.getScore() * rankingProperties.getCarryOverWeight()  // 가중치 적용
                ))
                .collect(Collectors.toSet());
        rankingRepository.saveAll(tomorrowKey, rankings, rankingProperties.getTtlDuration());

        log.info("Top{} 캐리오버 완료: count={}, tomorrowKey={}", n, rankings.size(), tomorrowKey);
    }

    /**
     * 백그라운드 보강 (오늘 → 내일 키로 데이터 옮김)
     */
    public void backfillTomorrowKey(String todayKey, String tomorrowKey) {
        log.info("백그라운드 보강 시작: todayKey={}, tomorrowKey={}", todayKey, tomorrowKey);

        try (Cursor<ZSetOperations.TypedTuple<String>> cursor =
                     rankingRepository.scan(todayKey, 1000)) {

            while (cursor.hasNext()) {
                Set<Ranking> batch = new HashSet<>();

                for (int i = 0; i < 1000 && cursor.hasNext(); i++) {
                    ZSetOperations.TypedTuple<String> tuple = cursor.next();
                    if (tuple != null && tuple.getValue() != null && tuple.getScore() != null) {
                        batch.add(new Ranking(
                                RankingKeyUtils.parseProductId(tuple.getValue()),
                                tuple.getScore() * rankingProperties.getCarryOverWeight()
                        ));
                    }
                }

                if (!batch.isEmpty()) {
                    rankingRepository.saveAll(tomorrowKey, batch, rankingProperties.getTtlDuration());
                }
            }

            log.info("백그라운드 보강 완료: tomorrowKey={}", tomorrowKey);

        } catch (Exception e) {
            log.error("백그라운드 보강 실패: tomorrowKey={}", tomorrowKey, e);
        }
    }
}
