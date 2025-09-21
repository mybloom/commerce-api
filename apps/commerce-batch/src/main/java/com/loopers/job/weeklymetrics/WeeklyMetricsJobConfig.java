package com.loopers.job.weeklymetrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.infrastructure.weeklymetrics.ProductMetricsWeeklyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyMetricsJobConfig {

    public static final int CHUNK_SIZE = 2;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ProductMetricsWeeklyJpaRepository productMetricsWeeklyRepository;

    @Bean
    public Job weeklyMetricsJob(Step weeklyMetricsStep) {
        return new JobBuilder("weeklyMetricsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(weeklyMetricsStep)
                .build();
    }

    @Bean
    public Step weeklyMetricsStep(
            @Qualifier("productMetricsReader") ItemReader<ProductMetrics> reader,
            ItemProcessor<ProductMetrics, ProductMetrics> processor,
            ItemWriter<ProductMetrics> weeklyMetricsWriter
    ) {
        return new StepBuilder("weeklyMetricsStep", jobRepository)
                .<ProductMetrics, ProductMetrics>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(weeklyMetricsWriter)
                .allowStartIfComplete(true)
                .build();
    }


    // Processor만 유지 (Reader는 Factory에서 관리)
/*    @Bean
    @StepScope
    public ItemProcessor<ProductMetrics, ProductMetricsWeekly> productMetricsProcessor(
            @Value("#{jobParameters['toDate']}") String toDateStr
    ) {
        var to = java.time.LocalDate.parse(toDateStr);
        var from = to.minusDays(6);

        return metrics -> ProductMetricsWeekly.from(metrics, from, to);
    }*/

    @Bean
    @StepScope
    public ItemProcessor<ProductMetrics, ProductMetrics> productMetricsProcessor() {
        // 읽은 데이터를 그대로 Writer로 전달
        return item -> item;
    }
}
