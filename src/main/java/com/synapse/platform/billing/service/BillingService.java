package com.synapse.platform.billing.service;

import com.synapse.platform.billing.dto.request.ChargeRequest;
import com.synapse.platform.billing.dto.response.InvoiceResponse;
import com.synapse.platform.billing.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceResponse charge(ChargeRequest request) {
        // TODO: PG 연동 (W3 이벤트 발행 시 BillingChargeRequested 토픽 사용)
        return new InvoiceResponse(0L, request.userId(), request.amount(), "PENDING");
    }
}
