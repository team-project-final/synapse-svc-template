package com.synapse.knowledge.graph.repository;

import com.synapse.knowledge.graph.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {
}
