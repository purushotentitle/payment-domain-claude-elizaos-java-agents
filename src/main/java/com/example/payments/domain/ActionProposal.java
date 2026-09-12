package com.example.payments.domain;

public record ActionProposal(
        String type,
        String description,
        boolean requiresApproval,
        String approvalId
) {
    public ActionProposal(String type, String description, boolean requiresApproval) {
        this(type, description, requiresApproval, null);
    }

    public ActionProposal withApprovalId(String newApprovalId) {
        return new ActionProposal(type, description, requiresApproval, newApprovalId);
    }
}
