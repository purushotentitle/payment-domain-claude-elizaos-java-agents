package com.example.payments.agents;

import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public final class PaymentOrchestrationAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "payment-orchestrator";
    }

    @Override
    public String name() {
        return "Payment Orchestration Agent";
    }

    @Override
    public String description() {
        return "Routes authorisations, retry decisions, and payment capture proposals across gateways.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("gatewayHealth", context.paymentToolbox().gatewayHealth(request));
        evidence.put("riskSignals", context.paymentToolbox().riskSignals(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean degraded = evidence.toString().contains("degraded");
        boolean highValue = highValue(request);
        List<ActionProposal> actions = List.of(
                new ActionProposal("ROUTE_PAYMENT", degraded ? "Use backup gateway with same idempotency key." : "Use primary gateway.", false),
                new ActionProposal("APPLY_3DS", "Request 3DS challenge for high-value or elevated-risk authorisations.", highValue),
                new ActionProposal("CAPTURE_PAYMENT", "Capture only after gateway authorisation and risk checks pass.", true)
        );

        return new AgentResponse(
                id(),
                "Evaluated gateway health and payment routing options. " + reasoning,
                degraded ? "Route through backup gateway and retry once inside the configured retry window." : "Proceed through primary gateway with idempotency protection.",
                highValue ? "MEDIUM" : "LOW",
                highValue,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a payment orchestration specialist. Focus on gateway routing, retries, idempotency, SCA/3DS, and approval-gated capture.";
    }
}
