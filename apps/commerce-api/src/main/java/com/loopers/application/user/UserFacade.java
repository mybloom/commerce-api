package com.loopers.application.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;
    private final PointService pointService;

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

}
