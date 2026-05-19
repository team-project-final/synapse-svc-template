package com.synapse.engagement.gamification.infrastructure.persistence;

import com.synapse.engagement.gamification.application.port.BadgePort;
import com.synapse.engagement.gamification.domain.Badge;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class BadgePersistenceAdapter implements BadgePort {

    private final BadgeJpaRepository badgeRepo;

    BadgePersistenceAdapter(BadgeJpaRepository badgeRepo) {
        this.badgeRepo = badgeRepo;
    }

    @Override public Badge save(Badge badge) { return badgeRepo.save(badge); }
    @Override public List<Badge> findByUserId(Long userId) { return badgeRepo.findByUserId(userId); }
}
