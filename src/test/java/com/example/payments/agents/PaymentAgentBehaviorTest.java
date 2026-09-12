package com.example.payments.agents;

import com.example.payments.adapters.MockComplianceScreeningAdapter;
import com.example.payments.adapters.MockDisputeProviderAdapter;
import com.example.payments.adapters.MockFraudProviderAdapter;
import com.example.payments.adapters.MockPaymentGatewayAdapter;
import com.example.payments.adapters.MockRefundPolicyAdapter;
import com.example.payments.adapters.MockSettlementLedgerAdapter;
import com.example.payments.adapters.MockSubscriptionBillingAdapter;
import com.example.payments.anthropic.LocalHeuristicClaudeClient;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import com.example.payments.security.SensitiveDataRedactor;
import com.example.payments.tools.PaymentToolbox;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentAgentBehaviorTest {
    private final AgentContext context = new AgentContext(
            new LocalHeuristicClaudeClient(),
            new PaymentToolbox(
                    new MockFraudProviderAdapter(),
                    new MockPaymentGatewayAdapter(),
                    new MockSettlementLedgerAdapter(),
                    new MockDisputeProviderAdapter(),
                    new MockSubscriptionBillingAdapter(),
                    new MockComplianceScreeningAdapter(),
                    new MockRefundPolicyAdapter()
            ),
            new SensitiveDataRedactor()
    );

    @Test
    void fraudRiskAgentFlagsHighRiskPayment() {
        AgentResponse response = new FraudRiskAgent().handle(request(
                "Evaluate before capture. New device, AVS fail, high value order.",
                "pay_123",
                "2499.00",
                "mrc_high_value",
                "cust_new_device"
        ), context);

        assertThat(response.agentId()).isEqualTo("fraud-risk");
        assertThat(response.riskLevel()).isEqualTo("HIGH");
        assertThat(response.requiresApproval()).isTrue();
        assertThat(response.actions()).anySatisfy(action -> {
            assertThat(action.type()).isEqualTo("HOLD_CAPTURE");
            assertThat(action.requiresApproval()).isTrue();
        });
    }

    @Test
    void paymentOrchestratorUsesBackupGatewayWhenPrimaryIsDegraded() {
        AgentResponse response = new PaymentOrchestrationAgent().handle(request(
                "Gateway outage on primary processor. Use backup routing if safe.",
                "pay_orch_001",
                "1299.00",
                "mrc_high_value",
                "cust_repeat_buyer"
        ), context);

        assertThat(response.recommendation()).contains("backup gateway");
        assertThat(response.riskLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void chargebackAgentRecommendsRepresentmentWhenEvidenceExists() {
        AgentResponse response = new ChargebackDisputeAgent().handle(request(
                "Fraud chargeback but order was delivered and support contacted customer.",
                "chb_001",
                "319.99",
                "mrc_retail",
                "cust_dispute"
        ), context);

        assertThat(response.agentId()).isEqualTo("chargeback-dispute");
        assertThat(response.recommendation()).contains("representment");
    }

    @Test
    void reconciliationAgentFlagsLedgerMismatch() {
        AgentResponse response = new ReconciliationAgent().handle(request(
                "Gateway settlement file shows a mismatch and delayed payout.",
                "pay_789",
                "118.30",
                "mrc_standard",
                "cust_451"
        ), context);

        assertThat(response.riskLevel()).isEqualTo("MEDIUM");
        assertThat(response.recommendation()).contains("reconciliation case");
    }

    @Test
    void subscriptionBillingAgentEscalatesAfterThirdRetry() {
        AgentResponse response = new SubscriptionBillingAgent().handle(request(
                "Failed renewal, past due invoice, third retry already attempted.",
                "inv_456",
                "79.00",
                "mrc_enterprise_saas",
                "cust_900"
        ), context);

        assertThat(response.riskLevel()).isEqualTo("MEDIUM");
        assertThat(response.recommendation()).contains("Escalate");
    }

    @Test
    void kycAmlAgentRequiresReviewForSanctionsAndPepSignals() {
        AgentResponse response = new KycAmlComplianceAgent().handle(request(
                "High risk country review with PEP indicator and possible sanctions match.",
                "acct_review_001",
                "5000.00",
                "mrc_cross_border",
                "cust_blocked_review"
        ), context);

        assertThat(response.riskLevel()).isEqualTo("HIGH");
        assertThat(response.recommendation()).contains("compliance review");
    }

    @Test
    void refundAgentRequiresApprovalForHighValueRefund() {
        AgentResponse response = new RefundSupportAgent().handle(request(
                "Customer asks for a refund. Order is not final sale.",
                "pay_ref_001",
                "549.00",
                "mrc_retail",
                "cust_refund"
        ), context);

        assertThat(response.agentId()).isEqualTo("refund-support");
        assertThat(response.requiresApproval()).isTrue();
        assertThat(response.actions()).anySatisfy(action -> {
            assertThat(action.type()).isEqualTo("ISSUE_REFUND");
            assertThat(action.requiresApproval()).isTrue();
        });
    }

    private AgentRequest request(String message, String paymentId, String amount, String merchantId, String customerId) {
        return new AgentRequest(
                message,
                paymentId,
                new BigDecimal(amount),
                "USD",
                merchantId,
                customerId,
                Map.of()
        );
    }
}
