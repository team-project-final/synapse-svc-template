package com.synapse.knowledge.graph.infrastructure.persistence;

import com.synapse.knowledge.graph.domain.Edge;
import org.springframework.data.jpa.repository.JpaRepository;

interface EdgeJpaRepository extends JpaRepository<Edge, Long> {
}
