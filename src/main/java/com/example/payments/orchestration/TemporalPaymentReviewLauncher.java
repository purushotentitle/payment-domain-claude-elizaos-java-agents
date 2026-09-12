package com.example.payments.orchestration;

import com.example.payments.config.TemporalProperties;
import com.example.payments.domain.AgentRequest;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "temporal.client.enabled", havingValue = "true")
public final class TemporalPaymentReviewLauncher {
    private final WorkflowClient workflowClient;
    private final TemporalProperties properties;

    public TemporalPaymentReviewLauncher(TemporalProperties properties) {
        this.properties = properties;
        WorkflowServiceStubs serviceStubs = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(properties.resolvedTarget())
                        .build()
        );
        this.workflowClient = WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(properties.resolvedNamespace())
                        .build()
        );
    }

    public PaymentReviewStartResponse start(String agentId, AgentRequest request) {
        String workflowId = "payment-review-" + agentId + "-" + UUID.randomUUID();
        PaymentReviewWorkflow workflow = workflowClient.newWorkflowStub(
                PaymentReviewWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(properties.resolvedTaskQueue())
                        .setWorkflowExecutionTimeout(Duration.ofMinutes(30))
                        .build()
        );

        WorkflowExecution execution = WorkflowClient.start(workflow::reviewPayment, agentId, request);
        return new PaymentReviewStartResponse(
                "temporal",
                execution.getWorkflowId(),
                execution.getRunId(),
                properties.resolvedTaskQueue(),
                "STARTED",
                "Temporal workflow started. Use Temporal UI or client APIs to inspect durable progress."
        );
    }
}
