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
public final class RefundSupportAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "refund-support";
    }

    @Override
    public String name() {
        return "Refund Support Agent";
    }

    @Override
    public String description() {
        return "Checks refund policy, proposes customer messaging, and gates money-moving refund actions.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("refundPolicy", context.paymentToolbox().refundPolicy(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean managerApproval = evidence.toString().contains("requiresManagerApproval=true");
        boolean eligible = evidence.toString().contains("eligible=true");
        List<ActionProposal> actions = List.of(
                new ActionProposal("DRAFT_CUSTOMER_REPLY", eligible ? "Explain refund timeline and original payment rail." : "Explain policy exception path.", false),
                new ActionProposal("CREATE_REFUND_CASE", "Create case with order, payment, policy, and support transcript references.", false),
                new ActionProposal("ISSUE_REFUND", "Issue refund only after approval and gateway idempotency validation.", true)
        );

        return new AgentResponse(
                id(),
                "Checked refund eligibility, approval threshold, and refund rail. " + reasoning,
                eligible ? "Prepare refund case for approval." : "Do not refund automatically; route for exception review.",
                managerApproval || !eligible ? "MEDIUM" : "LOW",
                true,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a refund support payment agent. Be customer-aware, policy-grounded, and never execute refund movement without approval.";
    }
}
