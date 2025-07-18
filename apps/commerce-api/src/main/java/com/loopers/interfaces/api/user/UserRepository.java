package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByMemberId(String memberId);

    Optional<User> findById(Long id);
}
