package com.synapse.platform.notification.api.dto.response;

public record NotificationResponse(Long id, Long userId, String channel, String status) {
}
