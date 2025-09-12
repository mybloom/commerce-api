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


}
