package com.synapse.learning.card.application.port;

import com.synapse.learning.card.domain.Card;

import java.util.List;

public interface CardPort {
    Card save(Card card);
    List<Card> findAll();
}
