package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface SubscriptionBillingAdapter {
    Map<String, Object> subscriptionState(AgentRequest request);
}
