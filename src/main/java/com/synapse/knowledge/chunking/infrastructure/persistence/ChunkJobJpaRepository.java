package com.synapse.knowledge.chunking.infrastructure.persistence;

import com.synapse.knowledge.chunking.domain.ChunkJob;
import org.springframework.data.jpa.repository.JpaRepository;

interface ChunkJobJpaRepository extends JpaRepository<ChunkJob, Long> {
}
