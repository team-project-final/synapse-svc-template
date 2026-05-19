package com.synapse.platform.auth.infrastructure.persistence;

import com.synapse.platform.auth.application.port.UserPort;
import com.synapse.platform.auth.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class UserPersistenceAdapter implements UserPort {

    private final UserJpaRepository jpaRepository;

    UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }
}
