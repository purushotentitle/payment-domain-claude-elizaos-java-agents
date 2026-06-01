package com.example.payments.tools;

import com.example.payments.domain.AgentRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PaymentToolbox {
    public Map<String, Object> riskSignals(AgentRequest request) {
        Map<String, Object> signals = new LinkedHashMap<>();
        double amount = request.amount() == null ? 0.0 : request.amount().doubleValue();
        signals.put("amountRisk", amount >= 1000.0 ? "high" : amount >= 250.0 ? "medium" : "low");
        signals.put("newDevice", contains(request.customerId(), "new") || contains(request.message(), "new device"));
        signals.put("velocity", contains(request.merchantId(), "high") ? "elevated" : "normal");
        signals.put("avsResult", contains(request.message(), "avs fail") ? "fail" : "pass");
        signals.put("threeDsAvailable", !contains(request.message(), "moto"));
        return signals;
    }

    public Map<String, Object> gatewayHealth(AgentRequest request) {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("primaryGateway", contains(request.message(), "gateway outage") ? "degraded" : "healthy");
        health.put("backupGateway", "healthy");
        health.put("retryWindowSeconds", 90);
        health.put("idempotencyKeyRequired", true);
        return health;
    }

    public Map<String, Object> settlementBreaks(AgentRequest request) {
        Map<String, Object> breaks = new LinkedHashMap<>();
        breaks.put("ledgerMatch", !contains(request.message(), "mismatch"));
        breaks.put("gatewayBatchStatus", contains(request.message(), "delayed") ? "delayed" : "closed");
        breaks.put("unmatchedCount", contains(request.message(), "mismatch") ? 3 : 0);
        breaks.put("nextCutoff", "T+1 18:00");
        return breaks;
    }

    public Map<String, Object> disputeFacts(AgentRequest request) {
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("reasonCode", contains(request.message(), "fraud") ? "10.4 other fraud" : "13.1 merchandise not received");
        facts.put("deliveryProofAvailable", contains(request.message(), "delivered"));
        facts.put("customerContacted", contains(request.message(), "contacted"));
        facts.put("representmentDeadlineDays", 9);
        return facts;
    }

    public Map<String, Object> subscriptionState(AgentRequest request) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("invoicePastDue", contains(request.message(), "past due") || contains(request.message(), "failed renewal"));
        state.put("retryCount", contains(request.message(), "third retry") ? 3 : 1);
        state.put("accountTier", contains(request.merchantId(), "enterprise") ? "enterprise" : "standard");
        state.put("dunningTemplate", "payment_method_refresh");
        return state;
    }

    public Map<String, Object> complianceScreen(AgentRequest request) {
        Map<String, Object> screen = new LinkedHashMap<>();
        screen.put("sanctionsHit", contains(request.message(), "sanctions") || contains(request.customerId(), "blocked"));
        screen.put("pepIndicator", contains(request.message(), "pep"));
        screen.put("countryRisk", contains(request.message(), "high risk country") ? "high" : "standard");
        screen.put("enhancedDueDiligenceRequired", contains(request.message(), "sanctions") || contains(request.message(), "pep"));
        return screen;
    }

    public Map<String, Object> refundPolicy(AgentRequest request) {
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("eligible", !contains(request.message(), "final sale"));
        policy.put("requiresManagerApproval", request.amount() != null && request.amount().doubleValue() >= 500.0);
        policy.put("suggestedRail", "original_payment_method");
        policy.put("slaHours", 24);
        return policy;
    }

    private boolean contains(String source, String fragment) {
        return source != null && source.toLowerCase().contains(fragment.toLowerCase());
    }
}
