package com.synapse.learning.srs.controller;

import com.synapse.learning.srs.dto.request.ReviewCardRequest;
import com.synapse.learning.srs.dto.response.ReviewResultResponse;
import com.synapse.learning.srs.service.SrsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/srs")
public class SrsController {

    private final SrsService srsService;

    public SrsController(SrsService srsService) {
        this.srsService = srsService;
    }

    @PostMapping("/review")
    public ReviewResultResponse review(@RequestBody ReviewCardRequest request) {
        return srsService.review(request);
    }
}
