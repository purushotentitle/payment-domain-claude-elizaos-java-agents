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
public final class KycAmlComplianceAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "kyc-aml-compliance";
    }

    @Override
    public String name() {
        return "KYC AML Compliance Agent";
    }

    @Override
    public String description() {
        return "Screens compliance signals and proposes enhanced due diligence or account restrictions.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("complianceScreen", context.paymentToolbox().complianceScreen(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean reviewRequired = evidence.toString().contains("true") || evidence.toString().contains("high");
        List<ActionProposal> actions = List.of(
                new ActionProposal("REQUEST_DOCUMENTS", "Request missing identity, beneficial ownership, or source-of-funds evidence.", false),
                new ActionProposal("OPEN_EDD_REVIEW", "Open enhanced due diligence case for compliance analyst review.", reviewRequired),
                new ActionProposal("RESTRICT_ACCOUNT", "Restrict account only after compliance approval and policy confirmation.", true)
        );

        return new AgentResponse(
                id(),
                "Screened sanctions, PEP, country risk, and due-diligence indicators. " + reasoning,
                reviewRequired ? "Open compliance review before further processing." : "No enhanced due diligence required from current signals.",
                reviewRequired ? "HIGH" : "LOW",
                reviewRequired,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a payments KYC and AML compliance assistant. Flag risk for human review, avoid final legal conclusions, and preserve auditability.";
    }
}
