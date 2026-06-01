package com.example.payments;

import com.example.payments.agents.ChargebackDisputeAgent;
import com.example.payments.agents.FraudRiskAgent;
import com.example.payments.agents.KycAmlComplianceAgent;
import com.example.payments.agents.PaymentAgent;
import com.example.payments.agents.PaymentOrchestrationAgent;
import com.example.payments.agents.ReconciliationAgent;
import com.example.payments.agents.RefundSupportAgent;
import com.example.payments.agents.SubscriptionBillingAgent;
import com.example.payments.anthropic.AnthropicClaudeClient;
import com.example.payments.anthropic.ClaudeClient;
import com.example.payments.anthropic.LocalHeuristicClaudeClient;
import com.example.payments.api.AgentHttpServer;
import com.example.payments.domain.AgentContext;
import com.example.payments.tools.PaymentToolbox;

import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        ClaudeClient claudeClient = createClaudeClient();
        AgentContext context = new AgentContext(claudeClient, new PaymentToolbox());
        List<PaymentAgent> agents = List.of(
                new PaymentOrchestrationAgent(),
                new FraudRiskAgent(),
                new ChargebackDisputeAgent(),
                new ReconciliationAgent(),
                new SubscriptionBillingAgent(),
                new KycAmlComplianceAgent(),
                new RefundSupportAgent()
        );

        AgentHttpServer server = new AgentHttpServer(port, agents, context);
        server.start();
        System.out.printf("Payment agent backend running at http://localhost:%d%n", port);
    }

    private static ClaudeClient createClaudeClient() {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return new LocalHeuristicClaudeClient();
        }
        String model = System.getenv().getOrDefault("CLAUDE_MODEL", "claude-sonnet-4-20250514");
        return new AnthropicClaudeClient(apiKey, model);
    }
}
