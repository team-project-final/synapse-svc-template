package com.synapse.platform.billing.application;

import com.synapse.platform.billing.api.dto.request.ChargeRequest;
import com.synapse.platform.billing.api.dto.response.InvoiceResponse;
import com.synapse.platform.billing.application.port.EventPort;
import com.synapse.platform.billing.application.port.InvoicePort;
import com.synapse.platform.billing.domain.Invoice;
import com.synapse.platform.billing.domain.policy.ChargePolicy;
import com.synapse.platform.global.exception.BusinessException;
import com.synapse.platform.global.exception.ErrorCode;
import com.synapse.platform.global.kafka.event.BillingChargeRequested;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class BillingService {

    private final InvoicePort invoicePort;
    private final EventPort eventPort;

    public BillingService(InvoicePort invoicePort, EventPort eventPort) {
        this.invoicePort = invoicePort;
        this.eventPort = eventPort;
    }

    public InvoiceResponse charge(ChargeRequest request) {
        if (!ChargePolicy.isChargeable(request.amount(), request.currency())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 금액/통화");
        }
        Invoice invoice = invoicePort.save(new Invoice(request.userId(), request.amount(), "PENDING"));
        eventPort.publishChargeRequested(new BillingChargeRequested(
            UUID.randomUUID().toString(),
            invoice.getUserId(),
            invoice.getAmount(),
            request.currency(),
            Instant.now()
        ));
        return new InvoiceResponse(invoice.getId(), invoice.getUserId(), invoice.getAmount(), invoice.getStatus());
    }
}
