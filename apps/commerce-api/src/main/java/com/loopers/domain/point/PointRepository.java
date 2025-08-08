package com.loopers.domain.point;


import java.util.Optional;

public interface PointRepository {
    Optional<Point> findById(Long id);

    Optional<Point> findByUserId(Long userId);
    Point save(Point point);

    Optional<Point> findByUserIdWithLock(Long userId);
}
