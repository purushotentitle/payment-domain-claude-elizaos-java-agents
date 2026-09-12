type PaymentAgentRequest = {
  message: string;
  paymentId?: string;
  amount: number;
  currency?: string;
  merchantId?: string;
  customerId?: string;
  metadata?: Record<string, string>;
};

type ActionProposal = {
  type: string;
  description: string;
  requiresApproval: boolean;
  approvalId?: string;
};

type PaymentAgentResponse = {
  agentId: string;
  summary: string;
  recommendation: string;
  riskLevel: string;
  requiresApproval: boolean;
  actions: ActionProposal[];
  evidence: Record<string, unknown>;
};

const DEFAULT_BACKEND_URL = "http://localhost:8080";

export const javaPaymentAgentAction = {
  name: "CALL_JAVA_PAYMENT_AGENT",
  similes: ["PAYMENT_AGENT", "PAYMENT_REVIEW", "RISK_REVIEW"],
  description: "Calls the Java payment-agent backend and returns an approval-gated recommendation.",

  validate: async () => true,

  handler: async (_runtime: unknown, message: { content?: { text?: string } }, _state: unknown, options: Record<string, unknown>, callback?: (response: { text: string }) => void) => {
    const agentId = String(options.agentId ?? "fraud-risk");
    const backendUrl = String(options.backendUrl ?? process.env.JAVA_PAYMENT_AGENT_URL ?? DEFAULT_BACKEND_URL);
    const apiKey = process.env.JAVA_PAYMENT_AGENT_API_KEY;

    const request: PaymentAgentRequest = {
      message: message.content?.text ?? "Review this payment.",
      paymentId: String(options.paymentId ?? "pay_elizaos_demo"),
      amount: Number(options.amount ?? 199.99),
      currency: String(options.currency ?? "USD"),
      merchantId: String(options.merchantId ?? "mrc_demo"),
      customerId: String(options.customerId ?? "cust_demo")
    };

    const headers: Record<string, string> = {
      "Content-Type": "application/json"
    };
    if (apiKey) {
      headers["X-API-Key"] = apiKey;
    }

    const response = await fetch(`${backendUrl}/agents/${agentId}/invoke`, {
      method: "POST",
      headers,
      body: JSON.stringify(request)
    });

    if (!response.ok) {
      throw new Error(`Java payment agent failed: ${response.status} ${await response.text()}`);
    }

    const result = await response.json() as PaymentAgentResponse;
    const approvalText = result.requiresApproval ? "Approval is required before execution." : "No approval is required for the recommended next step.";

    callback?.({
      text: [
        `Agent: ${result.agentId}`,
        `Risk: ${result.riskLevel}`,
        `Recommendation: ${result.recommendation}`,
        approvalText
      ].join("\n")
    });

    return result;
  }
};
