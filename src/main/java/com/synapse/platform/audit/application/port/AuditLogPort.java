package com.synapse.platform.audit.application.port;

import com.synapse.platform.audit.domain.AuditLog;

import java.util.List;

public interface AuditLogPort {
    AuditLog save(AuditLog auditLog);
    List<AuditLog> findAll();
}
