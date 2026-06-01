package com.example.payments.agents;

import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;

import java.util.Map;

abstract class BasePaymentAgent implements PaymentAgent {
    protected String askClaude(AgentContext context, String systemPrompt, AgentRequest request, Map<String, Object> evidence) {
        String userPrompt = """
                Payment request:
                message=%s
                paymentId=%s
                amount=%s
                currency=%s
                merchantId=%s
                customerId=%s
                evidence=%s

                Return concise payment-operations reasoning. Do not claim that money was moved.
                """
                .formatted(
                        nullToEmpty(request.message()),
                        nullToEmpty(request.paymentId()),
                        request.amount(),
                        nullToEmpty(request.currency()),
                        nullToEmpty(request.merchantId()),
                        nullToEmpty(request.customerId()),
                        evidence
                );
        try {
            return context.claudeClient().complete(systemPrompt, userPrompt);
        } catch (Exception ex) {
            return "Claude call unavailable: " + ex.getMessage();
        }
    }

    protected boolean highValue(AgentRequest request) {
        return request.amount() != null && request.amount().doubleValue() >= 1000.0;
    }

    protected String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
