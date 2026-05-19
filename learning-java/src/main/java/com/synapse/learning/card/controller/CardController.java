package com.synapse.learning.card.controller;

import com.synapse.learning.card.dto.request.CreateCardRequest;
import com.synapse.learning.card.dto.response.CardResponse;
import com.synapse.learning.card.service.CardService;
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
    public CardResponse create(@RequestBody CreateCardRequest request) {
        return cardService.create(request);
    }

    @GetMapping
    public List<CardResponse> list() {
        return cardService.findAll();
    }
}
