package com.synapse.platform.notification.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(
    @NotNull Long userId,
    @NotBlank String channel,
    @NotBlank String payload
) {
}
