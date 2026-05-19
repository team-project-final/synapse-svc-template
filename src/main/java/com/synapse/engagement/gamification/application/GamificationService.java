package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.api.dto.response.BadgeResponse;
import com.synapse.engagement.gamification.api.dto.response.UserScoreResponse;
import com.synapse.engagement.gamification.application.port.BadgePort;
import com.synapse.engagement.gamification.application.port.PointPort;
import com.synapse.engagement.gamification.domain.Badge;
import com.synapse.engagement.gamification.domain.Point;
import com.synapse.engagement.gamification.domain.policy.PointPolicy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamificationService {

    private final PointPort pointPort;
    private final BadgePort badgePort;

    public GamificationService(PointPort pointPort, BadgePort badgePort) {
        this.pointPort = pointPort;
        this.badgePort = badgePort;
    }

    public UserScoreResponse scoreOf(Long userId) {
        long total = pointPort.findByUserId(userId).stream()
            .mapToLong(Point::getAmount).sum();
        return new UserScoreResponse(userId, total);
    }

    public List<BadgeResponse> badgesOf(Long userId) {
        return badgePort.findByUserId(userId).stream()
            .map(b -> new BadgeResponse(b.getId(), b.getUserId(), b.getCode())).toList();
    }

    public List<UserScoreResponse> topN(int n) {
        return List.of(); // TODO: 실제 구현에서 인프라 쿼리
    }

    public void awardForEvent(Long userId, String reason, int rawValue) {
        long amount = PointPolicy.pointsFor(reason, rawValue);
        pointPort.save(new Point(userId, amount, reason));
    }

    public void awardBadge(Long userId, String code) {
        badgePort.save(new Badge(userId, code));
    }
}
