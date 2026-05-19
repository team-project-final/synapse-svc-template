package com.synapse.learning.card.infrastructure.persistence;

import com.synapse.learning.card.application.port.CardPort;
import com.synapse.learning.card.domain.Card;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CardPersistenceAdapter implements CardPort {

    private final CardJpaRepository jpaRepository;

    CardPersistenceAdapter(CardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override public Card save(Card card) { return jpaRepository.save(card); }
    @Override public List<Card> findAll() { return jpaRepository.findAll(); }
}
