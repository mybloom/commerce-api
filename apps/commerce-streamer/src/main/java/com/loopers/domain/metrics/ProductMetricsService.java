package com.loopers.domain.metrics;

import com.loopers.domain.sharedkernel.ProductMetricsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class ProductMetricsService {
    private final ProductMetricsRepository productMetricsRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        // 이벤트 발행
        ProductMetricsEvent.Updated event = new ProductMetricsEvent.Updated(
                metrics.getId(),
                metrics.getProductId(),
                metrics.getMetricsDate()
        );
        eventPublisher.publishEvent(event);
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

            // 이벤트 발행
            ProductMetricsEvent.Updated event = new ProductMetricsEvent.Updated(
                    metrics.getId(),
                    metrics.getProductId(),
                    metrics.getMetricsDate()
            );
            eventPublisher.publishEvent(event);
        }
    }

    public List<ProductMetrics> findAllByIds(Set<Long> productMetricsIds) {
        return productMetricsRepository.findAllById(productMetricsIds.stream().toList());
    }
}
