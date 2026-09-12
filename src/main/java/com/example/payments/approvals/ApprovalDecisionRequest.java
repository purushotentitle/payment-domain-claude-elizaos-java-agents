package com.example.payments.approvals;

import jakarta.validation.constraints.NotBlank;

public record ApprovalDecisionRequest(
        @NotBlank(message = "reviewer is required")
        String reviewer,
        String notes
) {
}
