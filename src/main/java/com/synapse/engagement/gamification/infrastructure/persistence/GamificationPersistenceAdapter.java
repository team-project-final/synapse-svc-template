package com.synapse.engagement.gamification.infrastructure.persistence;

import com.synapse.engagement.gamification.application.port.BadgePort;
import com.synapse.engagement.gamification.application.port.PointPort;
import com.synapse.engagement.gamification.domain.Badge;
import com.synapse.engagement.gamification.domain.Point;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class GamificationPersistenceAdapter implements PointPort, BadgePort {

    private final PointJpaRepository pointRepo;
    private final BadgeJpaRepository badgeRepo;

    GamificationPersistenceAdapter(PointJpaRepository pointRepo, BadgeJpaRepository badgeRepo) {
        this.pointRepo = pointRepo;
        this.badgeRepo = badgeRepo;
    }

    @Override public Point save(Point point) { return pointRepo.save(point); }
    @Override public List<Point> findByUserId(Long userId) { return pointRepo.findByUserId(userId); }
    @Override public Badge save(Badge badge) { return badgeRepo.save(badge); }
    @Override public List<Badge> findByUserId(Long userId) { return badgeRepo.findByUserId(userId); }
}
