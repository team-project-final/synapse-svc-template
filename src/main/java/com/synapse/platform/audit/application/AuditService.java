package com.synapse.platform.audit.application;

import com.synapse.platform.audit.api.dto.response.AuditLogResponse;
import com.synapse.platform.audit.application.port.AuditLogPort;
import com.synapse.platform.audit.domain.AuditLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogPort auditLogPort;

    public AuditService(AuditLogPort auditLogPort) {
        this.auditLogPort = auditLogPort;
    }

    public List<AuditLogResponse> findAll() {
        return auditLogPort.findAll().stream()
            .map(log -> new AuditLogResponse(log.getId(), log.getAction(), log.getActorId()))
            .toList();
    }

    public void record(String action, Long actorId) {
        auditLogPort.save(new AuditLog(action, actorId));
    }
}
