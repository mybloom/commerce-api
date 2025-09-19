package com.loopers.job.monthlyranking;

import com.loopers.application.ranking.MonthlyRankingReader;
import com.loopers.application.ranking.MonthlyRankingWriter;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.weeklymetrics.ProductMetricsWeekly;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class MonthlyRankingJobConfig {

    public static final int CHUNK_SIZE = 50;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job monthlyRankingJob(Step monthlyRankingStep) {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(monthlyRankingStep)
                .build();
    }

    @Bean
    public Step monthlyRankingStep(
            MonthlyRankingReader monthlyRankingReader,
            ItemProcessor<ProductMetricsWeekly, RankingResult> monthlyRankingProcessor,
            MonthlyRankingWriter monthlyRankingWriter
    ) {
        return new StepBuilder("monthlyRankingStep", jobRepository)
                .<ProductMetricsWeekly, RankingResult>chunk(CHUNK_SIZE, transactionManager)
                .reader(monthlyRankingReader)
                .processor(monthlyRankingProcessor)
                .writer(monthlyRankingWriter)
                .allowStartIfComplete(true)
                .build();
    }
}
