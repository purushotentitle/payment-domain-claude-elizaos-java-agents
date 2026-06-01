package com.example.payments.agents;

import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChargebackDisputeAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "chargeback-dispute";
    }

    @Override
    public String name() {
        return "Chargeback Dispute Agent";
    }

    @Override
    public String description() {
        return "Prepares dispute evidence, deadline checks, and representment recommendations.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("disputeFacts", context.paymentToolbox().disputeFacts(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean evidenceReady = evidence.toString().contains("deliveryProofAvailable=true");
        List<ActionProposal> actions = List.of(
                new ActionProposal("COLLECT_EVIDENCE", "Collect receipt, customer communication, delivery proof, and refund history.", false),
                new ActionProposal("DRAFT_REPRESENTMENT", "Draft representment packet mapped to card-network reason code.", false),
                new ActionProposal("SUBMIT_REPRESENTMENT", "Submit only after legal, support, or payments reviewer approval.", true)
        );

        return new AgentResponse(
                id(),
                "Assessed dispute facts and evidence readiness. " + reasoning,
                evidenceReady ? "Prepare representment packet for approval." : "Collect missing evidence before filing representment.",
                evidenceReady ? "MEDIUM" : "HIGH",
                true,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a chargeback operations specialist. Map facts to evidence, deadlines, reason codes, and approval-gated representment.";
    }
}
