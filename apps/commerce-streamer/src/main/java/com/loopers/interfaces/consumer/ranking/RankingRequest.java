package com.loopers.interfaces.consumer.ranking;

import com.loopers.application.ranking.RankingInfo;
import com.loopers.domain.sharedkernel.ProductMetricsEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
public class RankingRequest {

    public record Create(
            String messageId,
            String handler,
            LocalDateTime publishedAt,
            Long productMetricsId,
            Long productId,
            LocalDate metricsDate
    ) {
        public static Create from(ConsumerRecord<String, Object> record, String handler) {
            String messageId = Optional.ofNullable(record.headers().lastHeader("messageId"))
                    .map(h -> new String(h.value(), StandardCharsets.UTF_8))
                    .orElse("");

            LocalDateTime publishedAt = Optional.ofNullable(record.headers().lastHeader("publishedAt"))
                    .map(h -> new String(h.value(), StandardCharsets.UTF_8))
                    .filter(s -> !s.isBlank())
                    .map(s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME))
                    .orElse(null);

            ProductMetricsEvent.Updated payload = (ProductMetricsEvent.Updated) record.value();

            return new Create(
                    messageId,
                    handler,
                    publishedAt,
                    payload.productMetricsId(),
                    payload.productId(),
                    payload.metricsDate()
            );
        }

        public RankingInfo.Create convertToInfo() {
            return new RankingInfo.Create(
                    messageId,
                    handler,
                    publishedAt,
                    productMetricsId,
                    productId,
                    metricsDate
            );
        }
    }
}
