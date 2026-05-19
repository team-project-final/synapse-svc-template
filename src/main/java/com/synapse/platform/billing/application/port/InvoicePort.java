package com.synapse.platform.billing.application.port;

import com.synapse.platform.billing.domain.Invoice;

import java.util.Optional;

public interface InvoicePort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
}
