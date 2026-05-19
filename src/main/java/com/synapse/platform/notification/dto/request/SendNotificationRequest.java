package com.synapse.platform.notification.dto.request;

public record SendNotificationRequest(Long userId, String channel, String payload) {
}
