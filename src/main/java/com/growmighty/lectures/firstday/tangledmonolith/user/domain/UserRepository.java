package com.growmighty.lectures.firstday.tangledmonolith.user.domain;

import java.util.Optional;

// DIP 구현, 도메인은 인프라 기술을 몰라야 한다.
public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
