package com.example.payments.approvals;

public final class ApprovalNotFoundException extends RuntimeException {
    public ApprovalNotFoundException(String approvalId) {
        super("Approval case not found: " + approvalId);
    }
}
