package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockComplianceScreeningAdapter implements ComplianceScreeningAdapter {
    @Override
    public Map<String, Object> complianceScreen(AgentRequest request) {
        Map<String, Object> screen = new LinkedHashMap<>();
        screen.put("sanctionsHit", PaymentTextMatcher.contains(request.message(), "sanctions")
                || PaymentTextMatcher.contains(request.customerId(), "blocked"));
        screen.put("pepIndicator", PaymentTextMatcher.contains(request.message(), "pep"));
        screen.put("countryRisk", PaymentTextMatcher.contains(request.message(), "high risk country") ? "high" : "standard");
        screen.put("enhancedDueDiligenceRequired", PaymentTextMatcher.contains(request.message(), "sanctions")
                || PaymentTextMatcher.contains(request.message(), "pep"));
        return screen;
    }
}
