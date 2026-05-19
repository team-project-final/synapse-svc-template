package com.synapse.engagement.gamification.application.port;

import com.synapse.engagement.gamification.domain.Badge;

import java.util.List;

public interface BadgePort {
    Badge save(Badge badge);
    List<Badge> findByUserId(Long userId);
}
