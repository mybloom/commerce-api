package com.loopers.application.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;
    private final PointService pointService;

    @Transactional
    public UserFacadeDto.SignUpResult signUp(final UserFacadeDto.SignUpCriteria signUpCriteria) {
        final User user = userService.save(
                signUpCriteria.memberId(),
                signUpCriteria.email(),
                signUpCriteria.birthDate(),
                Gender.valueOf(signUpCriteria.gender().name())
        );

        // 초기 포인트 생성
        pointService.createInitialPoint(user.getId());

        return UserFacadeDto.SignUpResult.from(user);
    }

    public UserFacadeDto.MyInfoCriteria retrieveMyInfo(Long id) {
        User user = userService.retrieveById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return UserFacadeDto.MyInfoCriteria.from(user);
    }
}
