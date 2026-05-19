package com.synapse.platform.billing.application.port;

import com.synapse.platform.global.kafka.event.BillingChargeRequested;

public interface EventPort {
    void publishChargeRequested(BillingChargeRequested event);
}
