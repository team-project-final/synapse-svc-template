package com.synapse.knowledge.graph.repository;

import com.synapse.knowledge.graph.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdgeRepository extends JpaRepository<Edge, Long> {
}
