package com.synapse.platform.notification.api;

import com.synapse.platform.global.response.ApiResponse;
import com.synapse.platform.notification.api.dto.request.SendNotificationRequest;
import com.synapse.platform.notification.api.dto.response.NotificationResponse;
import com.synapse.platform.notification.application.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ApiResponse<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest request) {
        return ApiResponse.ok(notificationService.send(request));
    }
}
