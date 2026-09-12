package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface ComplianceScreeningAdapter {
    Map<String, Object> complianceScreen(AgentRequest request);
}
