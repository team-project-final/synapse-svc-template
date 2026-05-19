package com.synapse.knowledge.chunking.repository;

import com.synapse.knowledge.chunking.entity.ChunkJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkJobRepository extends JpaRepository<ChunkJob, Long> {
}
