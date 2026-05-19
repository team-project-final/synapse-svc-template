package com.synapse.learning.card.application;

import com.synapse.learning.card.api.dto.request.CreateCardRequest;
import com.synapse.learning.card.api.dto.response.CardResponse;
import com.synapse.learning.card.application.port.CardPort;
import com.synapse.learning.card.domain.Card;
import com.synapse.learning.card.domain.policy.CardValidationPolicy;
import com.synapse.learning.global.exception.BusinessException;
import com.synapse.learning.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    private final CardPort cardPort;

    public CardService(CardPort cardPort) {
        this.cardPort = cardPort;
    }

    public CardResponse create(CreateCardRequest request) {
        if (!CardValidationPolicy.isValid(request.frontText(), request.backText())) {
            throw new BusinessException(ErrorCode.CARD_TEXT_REQUIRED);
        }
        Card saved = cardPort.save(new Card(request.ownerId(), request.frontText(), request.backText()));
        return toResponse(saved);
    }

    public List<CardResponse> findAll() {
        return cardPort.findAll().stream().map(this::toResponse).toList();
    }

    private CardResponse toResponse(Card c) {
        return new CardResponse(c.getId(), c.getOwnerId(), c.getFrontText(), c.getBackText());
    }
}
