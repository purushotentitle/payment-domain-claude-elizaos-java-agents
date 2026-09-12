package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface RefundPolicyAdapter {
    Map<String, Object> refundPolicy(AgentRequest request);
}
