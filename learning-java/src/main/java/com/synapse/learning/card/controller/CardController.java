package com.synapse.learning.card.controller;

import com.synapse.learning.card.dto.request.CreateCardRequest;
import com.synapse.learning.card.dto.response.CardResponse;
import com.synapse.learning.card.service.CardService;
import com.synapse.learning.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ApiResponse<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        return ApiResponse.ok(cardService.create(request));
    }

    @GetMapping
    public ApiResponse<List<CardResponse>> list() {
        return ApiResponse.ok(cardService.findAll());
    }
}
