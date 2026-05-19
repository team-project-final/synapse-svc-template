package com.synapse.platform.auth.infrastructure.persistence;

import com.synapse.platform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository — 인프라 내부에서만 사용.
 * application 계층은 UserPort 인터페이스만 의존.
 */
interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
