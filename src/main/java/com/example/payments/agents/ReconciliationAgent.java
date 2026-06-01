package com.example.payments.agents;

import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReconciliationAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "reconciliation";
    }

    @Override
    public String name() {
        return "Reconciliation Agent";
    }

    @Override
    public String description() {
        return "Investigates gateway, ledger, payout, and settlement mismatches.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("settlementBreaks", context.paymentToolbox().settlementBreaks(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean mismatch = evidence.toString().contains("ledgerMatch=false");
        List<ActionProposal> actions = List.of(
                new ActionProposal("OPEN_RECON_CASE", "Create a reconciliation case with gateway batch, ledger entry, and payout references.", false),
                new ActionProposal("POST_ADJUSTMENT", "Post accounting adjustment only after finance approval.", true),
                new ActionProposal("ESCALATE_GATEWAY", "Escalate delayed or missing settlement files to gateway operations.", mismatch)
        );

        return new AgentResponse(
                id(),
                "Compared ledger, batch, and payout settlement status. " + reasoning,
                mismatch ? "Open reconciliation case and block automated adjustment." : "No break detected; continue settlement monitoring.",
                mismatch ? "MEDIUM" : "LOW",
                mismatch,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a payment reconciliation analyst. Identify settlement breaks, avoid unsupported accounting changes, and propose auditable next steps.";
    }
}
