package com.synapse.platform.notification.controller;

import com.synapse.platform.notification.dto.request.SendNotificationRequest;
import com.synapse.platform.notification.dto.response.NotificationResponse;
import com.synapse.platform.notification.service.NotificationService;
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
    public NotificationResponse send(@RequestBody SendNotificationRequest request) {
        return notificationService.send(request);
    }
}
