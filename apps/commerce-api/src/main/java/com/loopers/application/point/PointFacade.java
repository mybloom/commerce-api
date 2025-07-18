package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.point.PointV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final UserService userService;
    private final PointService pointService;

    @Transactional
    public PointFacadeDto.ChargeResult charge(final Long userId, final PointV1Dto.ChargeRequest chargeRequest) {
        userService.retrieveById(userId);
        final Long balance = pointService.charge(userId, chargeRequest.amount());

        return new PointFacadeDto.ChargeResult(balance);
    }

    public PointFacadeDto.RetrieveResult retrieve(final Long userId) {
        Point point = pointService.retrieve(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return new PointFacadeDto.RetrieveResult(point.getAmount());
    }
}
