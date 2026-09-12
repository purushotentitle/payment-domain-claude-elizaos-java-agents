package com.example.payments.orchestration;

import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface PaymentAgentActivities {
    @ActivityMethod
    AgentResponse invokeAgent(String agentId, AgentRequest request);
}
