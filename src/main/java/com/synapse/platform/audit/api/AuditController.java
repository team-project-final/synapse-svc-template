package com.synapse.platform.audit.api;

import com.synapse.platform.audit.api.dto.response.AuditLogResponse;
import com.synapse.platform.audit.application.AuditService;
import com.synapse.platform.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit/logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> list() {
        return ApiResponse.ok(auditService.findAll());
    }
}
