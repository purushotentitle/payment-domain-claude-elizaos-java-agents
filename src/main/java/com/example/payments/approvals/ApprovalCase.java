package com.example.payments.approvals;

import java.time.Instant;

public record ApprovalCase(
        String id,
        String agentId,
        String actionType,
        String description,
        ApprovalStatus status,
        String paymentId,
        String merchantId,
        String customerId,
        Instant createdAt,
        Instant decidedAt,
        String reviewer,
        String notes
) {
    public ApprovalCase withDecision(ApprovalStatus newStatus, String newReviewer, String newNotes) {
        return new ApprovalCase(
                id,
                agentId,
                actionType,
                description,
                newStatus,
                paymentId,
                merchantId,
                customerId,
                createdAt,
                Instant.now(),
                newReviewer,
                newNotes
        );
    }
}
