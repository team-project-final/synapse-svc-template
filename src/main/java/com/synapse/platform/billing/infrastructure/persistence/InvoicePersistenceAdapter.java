package com.synapse.platform.billing.infrastructure.persistence;

import com.synapse.platform.billing.application.port.InvoicePort;
import com.synapse.platform.billing.domain.Invoice;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class InvoicePersistenceAdapter implements InvoicePort {

    private final InvoiceJpaRepository jpaRepository;

    InvoicePersistenceAdapter(InvoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return jpaRepository.save(invoice);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
