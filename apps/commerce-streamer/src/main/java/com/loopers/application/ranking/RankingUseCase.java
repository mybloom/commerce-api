package com.loopers.application.ranking;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
public class RankingUseCase {
    private final ProductMetricsService productMetricsService;
    private final RankingService rankingService;

    public void rank(List<RankingInfo.Create> info) {
        // 1. product_metrics 조회
        final List<ProductMetrics> productMetrics = productMetricsService.findAllByIds(
                info.stream()
                        .map(RankingInfo.Create::productMetricsId)
                        .collect(Collectors.toSet()) //여기서 같은 productMetricsId 는 하나로 zip(distinct) 된다.
                // ->멱등처리도 안해도 된다. 최신것만 처리하면 되므로
        );

        List<RankingCommand.ProductMetrics> commands = productMetrics.stream()
                .map(pm -> new RankingCommand.ProductMetrics(
                        pm.getId(),
                        pm.getProductId(),
                        pm.getMetricsDate(),
                        pm.getLikeCount(),
                        pm.getPurchaseCount(),
                        pm.getViewCount()
                ))
                .toList();
        rankingService.rank(commands);
    }
}
