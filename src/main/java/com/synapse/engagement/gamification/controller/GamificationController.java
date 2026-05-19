package com.synapse.engagement.gamification.controller;

import com.synapse.engagement.gamification.dto.response.BadgeResponse;
import com.synapse.engagement.gamification.dto.response.UserScoreResponse;
import com.synapse.engagement.gamification.service.GamificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/users/{userId}/score")
    public UserScoreResponse score(@PathVariable Long userId) {
        return gamificationService.scoreOf(userId);
    }

    @GetMapping("/users/{userId}/badges")
    public List<BadgeResponse> badges(@PathVariable Long userId) {
        return gamificationService.badgesOf(userId);
    }

    @GetMapping("/leaderboard")
    public List<UserScoreResponse> leaderboard() {
        return gamificationService.topN(10);
    }
}
