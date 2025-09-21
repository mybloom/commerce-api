package com.loopers.application.weeklymetrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.weeklymetrics.WeeklyMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyMetricsWriter implements ItemWriter<ProductMetrics>, StepExecutionListener {

    private final WeeklyMetricsService weeklyMetricsService;
    private LocalDate to;
    private LocalDate from;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String toDateStr = stepExecution.getJobParameters().getString("toDate");
        this.to = LocalDate.parse(toDateStr);
        this.from = to.minusDays(6);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

    @Override
    public void write(Chunk<? extends ProductMetrics> items) {
        weeklyMetricsService.aggregateAndSave((List<ProductMetrics>) items.getItems(), from, to);
    }
}
