package com.example.payments.orchestration;

import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PaymentReviewWorkflow {
    @WorkflowMethod
    AgentResponse reviewPayment(String agentId, AgentRequest request);
}
