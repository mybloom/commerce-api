package com.loopers.domain.point;

import com.loopers.domain.commonvo.Money;
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
                .orElseGet(() -> pointRepository.save(Point.createInitial(userId)));
    }

    @Transactional
    public Point charge(final Long userId, final Long amount) {
        final Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        point.charge(Money.of(amount));

        return pointRepository.save(point);
    }

    @Transactional(readOnly = true)
    public Optional<Point> retrieve(final Long userId) {
        return pointRepository.findByUserId(userId);
    }

    public void checkSufficientBalance(final Long userId, final Money paymentAmount) {
        final Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.CONFLICT, "잔액이 부족합니다."));

        if (point.balance().isLessThan(paymentAmount)) {
            throw new CoreException(ErrorType.CONFLICT, "보유 포인트가 충분하지 않습니다.");
        }
    }
}
