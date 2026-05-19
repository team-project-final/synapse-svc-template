package com.synapse.engagement.gamification.infrastructure.persistence;

import com.synapse.engagement.gamification.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PointJpaRepository extends JpaRepository<Point, Long> {
    List<Point> findByUserId(Long userId);
}
