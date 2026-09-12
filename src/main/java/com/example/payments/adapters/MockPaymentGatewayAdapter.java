package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockPaymentGatewayAdapter implements PaymentGatewayAdapter {
    @Override
    public Map<String, Object> gatewayHealth(AgentRequest request) {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("primaryGateway", PaymentTextMatcher.contains(request.message(), "gateway outage") ? "degraded" : "healthy");
        health.put("backupGateway", "healthy");
        health.put("retryWindowSeconds", 90);
        health.put("idempotencyKeyRequired", true);
        return health;
    }
}
