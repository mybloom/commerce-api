package com.loopers.application.ranking;

import com.loopers.domain.weeklymetrics.ProductMetricsWeekly;
import com.loopers.infrastructure.weeklymetrics.ProductMetricsWeeklyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyRankingReader implements ItemReader<ProductMetricsWeekly> {

    private final ProductMetricsWeeklyJpaRepository repository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateStr;

    private RepositoryItemReader<ProductMetricsWeekly> delegate;

    private boolean initialized = false;

    private void init() {
        LocalDate targetDate = LocalDate.parse(targetDateStr);

        List<LocalDate> windowEndDates = List.of(
                targetDate.minusWeeks(3),
                targetDate.minusWeeks(2),
                targetDate.minusWeeks(1),
                targetDate
        );

        this.delegate = new RepositoryItemReaderBuilder<ProductMetricsWeekly>()
                .name("monthlyRankingReader")
                .repository(repository)
                .methodName("findAllByWindowEndIn")
                .arguments(List.of(windowEndDates))
                .pageSize(50)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

        initialized = true;
    }

    @Override
    public ProductMetricsWeekly read() throws Exception {
        if (!initialized) {
            init();
        }
        return delegate.read();
    }
}
