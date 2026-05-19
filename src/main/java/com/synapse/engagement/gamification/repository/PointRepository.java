package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
