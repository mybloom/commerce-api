package com.loopers.application.weeklymetrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.infrastructure.metrics.ProductMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductMetricsReaderFactory {

    public static final int PAGE_SIZE = 2;
    public static final int DAYS_TO_SUBTRACT = 6;

    private final ProductMetricsJpaRepository productMetricsRepository;

    @Bean(name = "productMetricsReader")
    @StepScope
    public ItemReader<ProductMetrics> productMetricsReader(
            @Value("#{jobParameters['toDate']}") String toDateStr
    ) {
        LocalDate to = LocalDate.parse(toDateStr, DateTimeFormatter.ISO_DATE);
        LocalDate from = to.minusDays(DAYS_TO_SUBTRACT);

        return new RepositoryItemReaderBuilder<ProductMetrics>()
                .name("productMetricsReader") // Bean 이름과 동일하게
                .repository(productMetricsRepository)
                .methodName("findAllByMetricsDateBetween")
                .arguments(Arrays.asList(from, to))
                .pageSize(PAGE_SIZE)
                .sorts(Map.of(
                        "productId", Sort.Direction.ASC,
                        "metricsDate", Sort.Direction.ASC
                ))
                .build();
    }
}
