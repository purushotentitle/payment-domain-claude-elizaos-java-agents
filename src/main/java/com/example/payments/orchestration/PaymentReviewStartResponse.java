package com.example.payments.orchestration;

public record PaymentReviewStartResponse(
        String orchestration,
        String workflowId,
        String runId,
        String taskQueue,
        String status,
        String message
) {
    public static PaymentReviewStartResponse temporalDisabled() {
        return new PaymentReviewStartResponse(
                "temporal",
                null,
                null,
                null,
                "DISABLED",
                "Temporal client is disabled. Set TEMPORAL_CLIENT_ENABLED=true and run a Temporal service to start durable workflows."
        );
    }
}
