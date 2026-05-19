package com.synapse.knowledge.chunking.infrastructure.persistence;

import com.synapse.knowledge.chunking.domain.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

interface ChunkJpaRepository extends JpaRepository<Chunk, Long> {
}
