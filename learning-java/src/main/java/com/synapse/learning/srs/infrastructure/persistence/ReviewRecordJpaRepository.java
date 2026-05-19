package com.synapse.learning.srs.infrastructure.persistence;

import com.synapse.learning.srs.domain.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewRecordJpaRepository extends JpaRepository<ReviewRecord, Long> {
}
