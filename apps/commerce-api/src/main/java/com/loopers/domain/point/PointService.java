package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    @Transactional
    public Point createInitialPoint(final Long userId) {
        return pointRepository.findByUserId(userId)
                .orElseGet(() -> pointRepository.save(new Point(userId)));
    }

    @Transactional
    public Long charge(final Long userId, final Long amount) {
        final Point point = retrieve(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        final Long balance = point.charge(amount);
        pointRepository.updateAmountByUserId(userId, balance);

        return balance;
    }

    @Transactional(readOnly = true)
    public Optional<Point> retrieve(final Long userId) {
        return pointRepository.findByUserId(userId);
    }
}
