package com.synapse.platform.billing.infrastructure.persistence;

import com.synapse.platform.billing.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

interface InvoiceJpaRepository extends JpaRepository<Invoice, Long> {
}
