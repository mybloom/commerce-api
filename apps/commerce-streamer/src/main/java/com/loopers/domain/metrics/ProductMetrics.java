package com.loopers.domain.metrics;

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
        name = "product_metrics",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_date", columnNames = {"product_id", "metrics_date"})
        },
        indexes = {
                @Index(name = "idx_product", columnList = "product_id"),
                @Index(name = "idx_metrics_date", columnList = "metrics_date")
        }
)
public class ProductMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 상품의 지표인지 */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 어떤 날짜의 지표인지 (일자 기준, 시간 제외) */
    @Column(name = "metrics_date", nullable = false)
    private LocalDate metricsDate;

    /** 일별 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    /** 일별 판매 수 */
    @Column(name = "purchase_count", nullable = false)
    private Long purchaseCount;

    /** 일별 조회 수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    /** 생성 시각 */
    @Column(name = "created_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /** 수정 시각 */
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

    // ============================
    // 정적 팩토리 메서드
    // ============================
    public static ProductMetrics create(Long productId, LocalDate metricsDate) {
        return ProductMetrics.builder()
                .productId(productId)
                .metricsDate(metricsDate)
                .likeCount(0L)
                .purchaseCount(0L)
                .viewCount(0L)
                .build();
    }

    // ============================
    // 도메인 메서드 (누적 증가)
    // ============================
    public void increaseLike(long count) {
        this.likeCount += count;
    }

    public void decreaseLike(long count) {
        this.likeCount -= count;
    }

    public void increasePurchase(long count) {
        this.purchaseCount += count;
    }

    public void increaseView(long count) {
        this.viewCount += count;
    }
}
