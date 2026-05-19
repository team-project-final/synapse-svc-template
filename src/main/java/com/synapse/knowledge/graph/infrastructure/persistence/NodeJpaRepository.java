package com.synapse.knowledge.graph.infrastructure.persistence;

import com.synapse.knowledge.graph.domain.Node;
import org.springframework.data.jpa.repository.JpaRepository;

interface NodeJpaRepository extends JpaRepository<Node, Long> {
}
