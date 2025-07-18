package com.loopers.domain.user;

import com.loopers.interfaces.api.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User save(String memberId, String email, String birthDate, Gender gender) {
        if(userRepository.existsByMemberId(memberId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 사용자입니다.");
        }

        User user = userRepository.save(new User(memberId, email, birthDate, gender));
        return user;
    }

}
