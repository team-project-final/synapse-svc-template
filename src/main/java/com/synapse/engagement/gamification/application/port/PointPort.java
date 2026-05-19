package com.synapse.engagement.gamification.application.port;

import com.synapse.engagement.gamification.domain.Point;

import java.util.List;

public interface PointPort {
    Point save(Point point);
    List<Point> findByUserId(Long userId);
}
