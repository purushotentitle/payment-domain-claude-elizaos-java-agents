package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface PaymentGatewayAdapter {
    Map<String, Object> gatewayHealth(AgentRequest request);
}
