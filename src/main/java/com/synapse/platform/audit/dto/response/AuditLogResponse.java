package com.synapse.platform.audit.dto.response;

public record AuditLogResponse(Long id, String action, Long actorId) {
}
