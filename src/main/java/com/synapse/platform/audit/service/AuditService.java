package com.synapse.platform.audit.service;

import com.synapse.platform.audit.dto.response.AuditLogResponse;
import com.synapse.platform.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAll().stream()
            .map(log -> new AuditLogResponse(log.getId(), log.getAction(), log.getActorId()))
            .toList();
    }
}
