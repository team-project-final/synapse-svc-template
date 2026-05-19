package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
