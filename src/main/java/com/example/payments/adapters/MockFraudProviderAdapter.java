package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockFraudProviderAdapter implements FraudProviderAdapter {
    @Override
    public Map<String, Object> riskSignals(AgentRequest request) {
        Map<String, Object> signals = new LinkedHashMap<>();
        double amount = request.amount() == null ? 0.0 : request.amount().doubleValue();
        signals.put("amountRisk", amount >= 1000.0 ? "high" : amount >= 250.0 ? "medium" : "low");
        signals.put("newDevice", PaymentTextMatcher.contains(request.customerId(), "new")
                || PaymentTextMatcher.contains(request.message(), "new device"));
        signals.put("velocity", PaymentTextMatcher.contains(request.merchantId(), "high") ? "elevated" : "normal");
        signals.put("avsResult", PaymentTextMatcher.contains(request.message(), "avs fail") ? "fail" : "pass");
        signals.put("threeDsAvailable", !PaymentTextMatcher.contains(request.message(), "moto"));
        return signals;
    }
}
