package com.loopers.domain.weeklymetrics;

import com.loopers.domain.metrics.ProductMetrics;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "product_metrics_weekly",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_window_end",
                        columnNames = {"product_id", "window_end"}
                )
        },
        indexes = {
                @Index(name = "idx_product", columnList = "product_id"),
                @Index(name = "idx_window_end", columnList = "window_end")
        }
)
public class ProductMetricsWeekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품 ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 집계 윈도우 시작일 */
    @Column(name = "window_start", nullable = false)
    private LocalDate windowStart;

    /** 집계 윈도우 종료일 (= 기준일) */
    @Column(name = "window_end", nullable = false)
    private LocalDate windowEnd;

    /** 7일 합산 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    /** 7일 합산 주문 수 */
    @Column(name = "purchase_count", nullable = false)
    private Long purchaseCount;

    /** 7일 합산 조회 수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    /** 생성일 */
    @Column(name = "created_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /** 수정일 */
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductMetricsWeekly from(ProductMetricsWeeklyCommand.Create command) {
        return ProductMetricsWeekly.builder()
                .productId(command.productId())
                .windowStart(command.windowStart())
                .windowEnd(command.windowEnd())
                .likeCount(command.likeCount())
                .purchaseCount(command.purchaseCount())
                .viewCount(command.viewCount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
