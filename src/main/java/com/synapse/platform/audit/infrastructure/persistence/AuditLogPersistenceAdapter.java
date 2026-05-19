package com.synapse.platform.audit.infrastructure.persistence;

import com.synapse.platform.audit.application.port.AuditLogPort;
import com.synapse.platform.audit.domain.AuditLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class AuditLogPersistenceAdapter implements AuditLogPort {

    private final AuditLogJpaRepository jpaRepository;

    AuditLogPersistenceAdapter(AuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        return jpaRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findAll() {
        return jpaRepository.findAll();
    }
}
