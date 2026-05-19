package com.synapse.platform.auth.repository;

import com.synapse.platform.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
