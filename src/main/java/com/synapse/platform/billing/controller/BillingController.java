package com.synapse.platform.billing.controller;

import com.synapse.platform.billing.dto.request.ChargeRequest;
import com.synapse.platform.billing.dto.response.InvoiceResponse;
import com.synapse.platform.billing.service.BillingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/charge")
    public InvoiceResponse charge(@RequestBody ChargeRequest request) {
        return billingService.charge(request);
    }
}
