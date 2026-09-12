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
public final class SubscriptionBillingAgent extends BasePaymentAgent {
    @Override
    public String id() {
        return "subscription-billing";
    }

    @Override
    public String name() {
        return "Subscription Billing Agent";
    }

    @Override
    public String description() {
        return "Handles recurring payment failures, retry timing, dunning, and subscription state.";
    }

    @Override
    public AgentResponse handle(AgentRequest request, AgentContext context) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("subscriptionState", context.paymentToolbox().subscriptionState(request));
        String reasoning = askClaude(context, systemPrompt(), request, evidence);

        boolean thirdRetry = evidence.toString().contains("retryCount=3");
        List<ActionProposal> actions = List.of(
                new ActionProposal("SCHEDULE_RETRY", thirdRetry ? "Stop retries and move to grace-period decision." : "Schedule smart retry outside issuer quiet hours.", false),
                new ActionProposal("SEND_DUNNING", "Send payment-method refresh message using the configured dunning template.", false),
                new ActionProposal("PAUSE_SUBSCRIPTION", "Pause service after grace period only with merchant policy confirmation.", true)
        );

        return new AgentResponse(
                id(),
                "Reviewed invoice state, retry count, account tier, and dunning path. " + reasoning,
                thirdRetry ? "Escalate before pause or cancellation." : "Retry payment and notify customer.",
                thirdRetry ? "MEDIUM" : "LOW",
                thirdRetry,
                actions,
                evidence
        );
    }

    private String systemPrompt() {
        return "You are a subscription billing operations agent. Optimize retry timing, customer communication, and policy-safe subscription state changes.";
    }
}
