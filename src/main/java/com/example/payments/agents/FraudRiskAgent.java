package com.example.payments.agents;

import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FraudRiskAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "fraud-risk";
    }

    @Override
    public String name() {
        return "Fraud Risk Agent";
    }

    @Override
    public String description() {
        return "Scores payment risk and proposes challenge, hold, or manual review actions.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("riskSignals", context.paymentToolbox().riskSignals(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean highRisk = evidence.toString().contains("high") || evidence.toString().contains("fail");
        List<ActionProposal> actions = List.of(
                new ActionProposal("CHALLENGE_3DS", "Step up authentication before capture when 3DS is available.", false),
                new ActionProposal("HOLD_CAPTURE", "Hold capture until fraud operations review clears the payment.", highRisk),
                new ActionProposal("BLOCK_CUSTOMER", "Block customer only if policy and human review confirm account takeover or synthetic identity.", true)
        );

        return new AgentResponse(
                id(),
                "Reviewed fraud signals, device context, velocity, AVS, and transaction value. " + reasoning,
                highRisk ? "Send to manual fraud review and hold capture." : "Approve with standard monitoring.",
                highRisk ? "HIGH" : "LOW",
                highRisk,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a fraud risk analyst for card and wallet payments. Be conservative, explain signals, and never execute blocking without approval.";
    }
}
