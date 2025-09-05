package com.loopers.domain.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@RequiredArgsConstructor
@Service
public class ProductMetricsService {
    private final ProductMetricsRepository productMetricsRepository;

    public void save(MetricsCommand.Create command) {
        LocalDate today = LocalDate.now(); // 일자 기준 집계

        // 1. 기존 metrics 조회
        ProductMetrics metrics = productMetricsRepository
                .findByProductIdAndMetricsDate(command.productId(), today)
                .orElseGet(() -> productMetricsRepository.save(
                                ProductMetrics.create(command.productId(), today)
                        )
                );

        // 2. 지표 누적
        switch (command.metricsEventType()) {
            case LIKE_ADDED -> metrics.increaseLike(1L);
            case LIKE_REMOVED -> metrics.decreaseLike(1L);
            case VIEWED -> metrics.increaseView(1L);
        }

        // 3. 저장 (신규 or 업데이트)
//        productMetricsRepository.save(metrics);
    }

    public void savePurchase(MetricsCommand.CreatePurchase command) {
        LocalDate today = LocalDate.now(); // 일자 기준 집계

        // 여러 Product 반복
        for (MetricsCommand.CreatePurchase.Product p : command.products()) {
            ProductMetrics metrics = productMetricsRepository
                    .findByProductIdAndMetricsDate(p.productId(), today)
                    .orElseGet(() -> productMetricsRepository.save(
                            ProductMetrics.create(p.productId(), today)
                    ));

            // 지표 누적
            metrics.increasePurchase(p.quantity());
        }
    }
}
