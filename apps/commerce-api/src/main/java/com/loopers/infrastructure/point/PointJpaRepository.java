package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByUserId(Long userId);

    @Modifying
    @Query("update Point p set p.amount = :amount where p.userId = :userId")
    int updateAmountByUserId(@Param("userId") Long userId, @Param("amount") Long amount);
}
