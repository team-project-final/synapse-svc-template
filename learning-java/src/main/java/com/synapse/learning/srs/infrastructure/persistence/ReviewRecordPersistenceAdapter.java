package com.synapse.learning.srs.infrastructure.persistence;

import com.synapse.learning.srs.application.port.ReviewRecordPort;
import com.synapse.learning.srs.domain.ReviewRecord;
import org.springframework.stereotype.Component;

@Component
class ReviewRecordPersistenceAdapter implements ReviewRecordPort {

    private final ReviewRecordJpaRepository jpaRepository;

    ReviewRecordPersistenceAdapter(ReviewRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override public ReviewRecord save(ReviewRecord record) { return jpaRepository.save(record); }
}
