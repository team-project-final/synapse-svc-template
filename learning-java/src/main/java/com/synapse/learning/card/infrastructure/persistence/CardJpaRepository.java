package com.synapse.learning.card.infrastructure.persistence;

import com.synapse.learning.card.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

interface CardJpaRepository extends JpaRepository<Card, Long> {
}
