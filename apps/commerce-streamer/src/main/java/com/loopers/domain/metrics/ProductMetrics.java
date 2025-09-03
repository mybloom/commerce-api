package com.loopers.domain.metrics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_metrics",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id"})
        }
)
@Entity
public class ProductMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long likeCount;
    private Long viewCount;
    private Long purchaseCount;

    public static ProductMetrics create(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        return ProductMetrics.builder()
                .productId(productId)
                .likeCount(0L)
                .viewCount(0L)
                .purchaseCount(0L)
                .build();
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
