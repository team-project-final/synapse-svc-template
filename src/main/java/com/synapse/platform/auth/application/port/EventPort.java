package com.synapse.platform.auth.application.port;

import com.synapse.platform.global.kafka.event.UserRegistered;

/**
 * Outbound port — 메시징. infrastructure/messaging이 구현.
 */
public interface EventPort {
    void publishUserRegistered(UserRegistered event);
}
