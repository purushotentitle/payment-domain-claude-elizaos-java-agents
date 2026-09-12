package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface DisputeProviderAdapter {
    Map<String, Object> disputeFacts(AgentRequest request);
}
