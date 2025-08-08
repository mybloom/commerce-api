package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductDslRepository {

    @Modifying
    @Query("UPDATE Product p SET p.likeCount = p.likeCount + 1 WHERE p.id = :productId")
    int updateLikeCountById(@Param("productId") Long productId);
}
