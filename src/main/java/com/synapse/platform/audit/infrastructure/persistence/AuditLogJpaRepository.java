package com.synapse.platform.audit.infrastructure.persistence;

import com.synapse.platform.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {
}
