package com.example.payments.orchestration;

import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public final class PaymentReviewWorkflowImpl implements PaymentReviewWorkflow {
    private final PaymentAgentActivities activities = Workflow.newActivityStub(
            PaymentAgentActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(60))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumInterval(Duration.ofSeconds(10))
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    @Override
    public AgentResponse reviewPayment(String agentId, AgentRequest request) {
        return activities.invokeAgent(agentId, request);
    }
}
