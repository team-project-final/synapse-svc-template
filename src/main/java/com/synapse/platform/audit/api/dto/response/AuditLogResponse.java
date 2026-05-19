package com.synapse.platform.audit.api.dto.response;

public record AuditLogResponse(Long id, String action, Long actorId) {
}
