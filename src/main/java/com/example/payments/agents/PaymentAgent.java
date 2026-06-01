package com.example.payments.agents;

import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;

public interface PaymentAgent {
    String id();

    String name();

    String description();

    AgentResponse handle(AgentRequest request, AgentContext context);
}
