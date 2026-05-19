package com.synapse.platform.auth.application.port;

import com.synapse.platform.auth.domain.User;

import java.util.Optional;

/**
 * Outbound port — 영속성. infrastructure/persistence가 구현.
 */
public interface UserPort {
    Optional<User> findByEmail(String email);
    User save(User user);
}
