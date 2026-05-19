package com.synapse.knowledge.chunking.repository;

import com.synapse.knowledge.chunking.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
}
