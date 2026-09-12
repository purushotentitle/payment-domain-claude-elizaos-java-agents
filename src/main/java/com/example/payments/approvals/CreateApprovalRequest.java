package com.example.payments.approvals;

import jakarta.validation.constraints.NotBlank;

public record CreateApprovalRequest(
        @NotBlank(message = "agentId is required")
        String agentId,

        @NotBlank(message = "actionType is required")
        String actionType,

        @NotBlank(message = "description is required")
        String description,

        String paymentId,
        String merchantId,
        String customerId
) {
}
