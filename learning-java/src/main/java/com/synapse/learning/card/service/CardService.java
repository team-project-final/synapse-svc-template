package com.synapse.learning.card.service;

import com.synapse.learning.card.dto.request.CreateCardRequest;
import com.synapse.learning.card.dto.response.CardResponse;
import com.synapse.learning.card.entity.Card;
import com.synapse.learning.card.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardResponse create(CreateCardRequest request) {
        Card saved = cardRepository.save(new Card(request.ownerId(), request.frontText(), request.backText()));
        return toResponse(saved);
    }

    public List<CardResponse> findAll() {
        return cardRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CardResponse toResponse(Card c) {
        return new CardResponse(c.getId(), c.getOwnerId(), c.getFrontText(), c.getBackText());
    }
}
