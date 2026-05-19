package com.synapse.engagement.gamification.infrastructure.persistence;

import com.synapse.engagement.gamification.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface BadgeJpaRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserId(Long userId);
}
