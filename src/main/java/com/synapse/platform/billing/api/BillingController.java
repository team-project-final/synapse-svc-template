package com.synapse.platform.billing.api;

import com.synapse.platform.billing.api.dto.request.ChargeRequest;
import com.synapse.platform.billing.api.dto.response.InvoiceResponse;
import com.synapse.platform.billing.application.BillingService;
import com.synapse.platform.global.response.ApiResponse;
import jakarta.validation.Valid;
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
    public ApiResponse<InvoiceResponse> charge(@Valid @RequestBody ChargeRequest request) {
        return ApiResponse.ok(billingService.charge(request));
    }
}
