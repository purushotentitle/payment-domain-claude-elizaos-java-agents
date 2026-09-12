package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockSubscriptionBillingAdapter implements SubscriptionBillingAdapter {
    @Override
    public Map<String, Object> subscriptionState(AgentRequest request) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("invoicePastDue", PaymentTextMatcher.contains(request.message(), "past due")
                || PaymentTextMatcher.contains(request.message(), "failed renewal"));
        state.put("retryCount", PaymentTextMatcher.contains(request.message(), "third retry") ? 3 : 1);
        state.put("accountTier", PaymentTextMatcher.contains(request.merchantId(), "enterprise") ? "enterprise" : "standard");
        state.put("dunningTemplate", "payment_method_refresh");
        return state;
    }
}
