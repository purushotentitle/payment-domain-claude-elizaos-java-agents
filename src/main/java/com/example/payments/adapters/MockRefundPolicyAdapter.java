package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockRefundPolicyAdapter implements RefundPolicyAdapter {
    @Override
    public Map<String, Object> refundPolicy(AgentRequest request) {
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("eligible", !PaymentTextMatcher.contains(request.message(), "final sale"));
        policy.put("requiresManagerApproval", request.amount() != null && request.amount().doubleValue() >= 500.0);
        policy.put("suggestedRail", "original_payment_method");
        policy.put("slaHours", 24);
        return policy;
    }
}
