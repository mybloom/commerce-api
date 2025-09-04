package com.loopers.infrastructure;

import com.loopers.domain.metrics.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {

    Optional<ProductMetrics> findByProductId(Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ProductMetrics p SET p.likeCount = p.likeCount + 1 WHERE p.productId = :productId")
    int increaseLikeCountAtomically(@Param("productId") Long productId);

    @Modifying //(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ProductMetrics p SET p.likeCount = p.likeCount - 1 WHERE p.productId = :productId")
    int decreaseLikeCountAtomically(@Param("productId") Long productId);

}
