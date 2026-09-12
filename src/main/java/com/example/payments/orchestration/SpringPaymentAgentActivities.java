package com.example.payments.orchestration;

import com.example.payments.agents.AgentService;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import org.springframework.stereotype.Component;

@Component
public final class SpringPaymentAgentActivities implements PaymentAgentActivities {
    private final AgentService agentService;

    public SpringPaymentAgentActivities(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public AgentResponse invokeAgent(String agentId, AgentRequest request) {
        return agentService.invoke(agentId, request);
    }
}
