package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.dto.response.BadgeResponse;
import com.synapse.engagement.gamification.dto.response.UserScoreResponse;
import com.synapse.engagement.gamification.entity.Badge;
import com.synapse.engagement.gamification.entity.Point;
import com.synapse.engagement.gamification.repository.BadgeRepository;
import com.synapse.engagement.gamification.repository.PointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamificationService {

    private final PointRepository pointRepository;
    private final BadgeRepository badgeRepository;

    public GamificationService(PointRepository pointRepository, BadgeRepository badgeRepository) {
        this.pointRepository = pointRepository;
        this.badgeRepository = badgeRepository;
    }

    public UserScoreResponse scoreOf(Long userId) {
        long total = pointRepository.findAll().stream()
            .filter(p -> p.getUserId().equals(userId))
            .mapToLong(Point::getAmount)
            .sum();
        return new UserScoreResponse(userId, total);
    }

    public List<BadgeResponse> badgesOf(Long userId) {
        return badgeRepository.findAll().stream()
            .filter(b -> b.getUserId().equals(userId))
            .map(b -> new BadgeResponse(b.getId(), b.getUserId(), b.getCode()))
            .toList();
    }

    public List<UserScoreResponse> topN(int n) {
        // TODO: 실제 구현에서는 SQL aggregation. W1 스텁.
        return List.of();
    }

    public void awardPoint(Long userId, long amount, String reason) {
        pointRepository.save(new Point(userId, amount, reason));
    }

    public void awardBadge(Long userId, String code) {
        badgeRepository.save(new Badge(userId, code));
    }
}
