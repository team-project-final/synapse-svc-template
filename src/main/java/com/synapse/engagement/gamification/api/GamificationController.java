package com.synapse.engagement.gamification.api;

import com.synapse.engagement.gamification.api.dto.response.BadgeResponse;
import com.synapse.engagement.gamification.api.dto.response.UserScoreResponse;
import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.global.response.ApiResponse;
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
    public ApiResponse<UserScoreResponse> score(@PathVariable Long userId) {
        return ApiResponse.ok(gamificationService.scoreOf(userId));
    }

    @GetMapping("/users/{userId}/badges")
    public ApiResponse<List<BadgeResponse>> badges(@PathVariable Long userId) {
        return ApiResponse.ok(gamificationService.badgesOf(userId));
    }

    @GetMapping("/leaderboard")
    public ApiResponse<List<UserScoreResponse>> leaderboard() {
        return ApiResponse.ok(gamificationService.topN(10));
    }
}
