package com.synapse.learning.srs.application.port;

import com.synapse.learning.srs.domain.ReviewRecord;

public interface ReviewRecordPort {
    ReviewRecord save(ReviewRecord record);
}
