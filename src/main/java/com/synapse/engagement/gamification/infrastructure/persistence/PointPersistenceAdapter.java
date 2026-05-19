package com.synapse.engagement.gamification.infrastructure.persistence;

import com.synapse.engagement.gamification.application.port.PointPort;
import com.synapse.engagement.gamification.domain.Point;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class PointPersistenceAdapter implements PointPort {

    private final PointJpaRepository pointRepo;

    PointPersistenceAdapter(PointJpaRepository pointRepo) {
        this.pointRepo = pointRepo;
    }

    @Override public Point save(Point point) { return pointRepo.save(point); }
    @Override public List<Point> findByUserId(Long userId) { return pointRepo.findByUserId(userId); }
}
