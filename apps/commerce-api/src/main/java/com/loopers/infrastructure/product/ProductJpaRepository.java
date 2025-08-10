package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductDslRepository {

    @Modifying
    @Query("UPDATE Product p SET p.likeCount = p.likeCount + 1 WHERE p.id = :productId")
    int updateLikeCountById(@Param("productId") Long productId);

    @Modifying
    @Query("update Product p set p.likeCount = p.likeCount - 1 where p.id = :productId and p.likeCount > 0")
    int decreaseLikeCountById(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.status = 'AVAILABLE'")
    List<Product> findAllValidWithPessimisticLock(@Param("ids") List<Long> ids);
}
