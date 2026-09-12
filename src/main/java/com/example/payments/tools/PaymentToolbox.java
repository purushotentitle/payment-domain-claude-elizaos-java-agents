package com.example.payments.tools;

import com.example.payments.adapters.ComplianceScreeningAdapter;
import com.example.payments.adapters.DisputeProviderAdapter;
import com.example.payments.adapters.FraudProviderAdapter;
import com.example.payments.adapters.PaymentGatewayAdapter;
import com.example.payments.adapters.RefundPolicyAdapter;
import com.example.payments.adapters.SettlementLedgerAdapter;
import com.example.payments.adapters.SubscriptionBillingAdapter;
import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class PaymentToolbox {
    private final FraudProviderAdapter fraudProviderAdapter;
    private final PaymentGatewayAdapter paymentGatewayAdapter;
    private final SettlementLedgerAdapter settlementLedgerAdapter;
    private final DisputeProviderAdapter disputeProviderAdapter;
    private final SubscriptionBillingAdapter subscriptionBillingAdapter;
    private final ComplianceScreeningAdapter complianceScreeningAdapter;
    private final RefundPolicyAdapter refundPolicyAdapter;

    public PaymentToolbox(
            FraudProviderAdapter fraudProviderAdapter,
            PaymentGatewayAdapter paymentGatewayAdapter,
            SettlementLedgerAdapter settlementLedgerAdapter,
            DisputeProviderAdapter disputeProviderAdapter,
            SubscriptionBillingAdapter subscriptionBillingAdapter,
            ComplianceScreeningAdapter complianceScreeningAdapter,
            RefundPolicyAdapter refundPolicyAdapter
    ) {
        this.fraudProviderAdapter = fraudProviderAdapter;
        this.paymentGatewayAdapter = paymentGatewayAdapter;
        this.settlementLedgerAdapter = settlementLedgerAdapter;
        this.disputeProviderAdapter = disputeProviderAdapter;
        this.subscriptionBillingAdapter = subscriptionBillingAdapter;
        this.complianceScreeningAdapter = complianceScreeningAdapter;
        this.refundPolicyAdapter = refundPolicyAdapter;
    }

    public Map<String, Object> riskSignals(AgentRequest request) {
        return fraudProviderAdapter.riskSignals(request);
    }

    public Map<String, Object> gatewayHealth(AgentRequest request) {
        return paymentGatewayAdapter.gatewayHealth(request);
    }

    public Map<String, Object> settlementBreaks(AgentRequest request) {
        return settlementLedgerAdapter.settlementBreaks(request);
    }

    public Map<String, Object> disputeFacts(AgentRequest request) {
        return disputeProviderAdapter.disputeFacts(request);
    }

    public Map<String, Object> subscriptionState(AgentRequest request) {
        return subscriptionBillingAdapter.subscriptionState(request);
    }

    public Map<String, Object> complianceScreen(AgentRequest request) {
        return complianceScreeningAdapter.complianceScreen(request);
    }

    public Map<String, Object> refundPolicy(AgentRequest request) {
        return refundPolicyAdapter.refundPolicy(request);
    }
}
