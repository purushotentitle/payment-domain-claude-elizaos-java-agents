package com.example.payments.domain;

public record ActionProposal(
        String type,
        String description,
        boolean requiresApproval
) {
}
