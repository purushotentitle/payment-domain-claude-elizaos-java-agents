package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface FraudProviderAdapter {
    Map<String, Object> riskSignals(AgentRequest request);
}
