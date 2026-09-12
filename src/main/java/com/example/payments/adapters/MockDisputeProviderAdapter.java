package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockDisputeProviderAdapter implements DisputeProviderAdapter {
    @Override
    public Map<String, Object> disputeFacts(AgentRequest request) {
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("reasonCode", PaymentTextMatcher.contains(request.message(), "fraud") ? "10.4 other fraud" : "13.1 merchandise not received");
        facts.put("deliveryProofAvailable", PaymentTextMatcher.contains(request.message(), "delivered"));
        facts.put("customerContacted", PaymentTextMatcher.contains(request.message(), "contacted"));
        facts.put("representmentDeadlineDays", 9);
        return facts;
    }
}
