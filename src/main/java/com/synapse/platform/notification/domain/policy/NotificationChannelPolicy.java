package com.synapse.platform.notification.domain.policy;

import java.util.Set;

public final class NotificationChannelPolicy {

    private static final Set<String> SUPPORTED = Set.of("EMAIL", "SMS", "PUSH", "WEBHOOK");

    private NotificationChannelPolicy() {}

    public static boolean isSupported(String channel) {
        return channel != null && SUPPORTED.contains(channel.toUpperCase());
    }
}
