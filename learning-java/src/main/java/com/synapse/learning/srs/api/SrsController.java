package com.synapse.learning.srs.api;

import com.synapse.learning.global.response.ApiResponse;
import com.synapse.learning.srs.api.dto.request.ReviewCardRequest;
import com.synapse.learning.srs.api.dto.response.ReviewResultResponse;
import com.synapse.learning.srs.application.SrsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/srs")
public class SrsController {

    private final SrsService srsService;

    public SrsController(SrsService srsService) {
        this.srsService = srsService;
    }

    @PostMapping("/review")
    public ApiResponse<ReviewResultResponse> review(@Valid @RequestBody ReviewCardRequest request) {
        return ApiResponse.ok(srsService.review(request));
    }
}
