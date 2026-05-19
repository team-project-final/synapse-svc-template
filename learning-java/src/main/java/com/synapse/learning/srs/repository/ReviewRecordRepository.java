package com.synapse.learning.srs.repository;

import com.synapse.learning.srs.entity.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
}
