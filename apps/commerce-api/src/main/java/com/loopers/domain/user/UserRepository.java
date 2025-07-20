package com.loopers.domain.user;


import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByMemberId(String memberId);

    Optional<User> findById(Long id);
}
