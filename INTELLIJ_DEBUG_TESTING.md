# IntelliJ Debug Testing Guide

Use this guide to run the Java backend in IntelliJ debug mode and test every payment agent with sample values.

## 1. Open the project in IntelliJ

1. Open IntelliJ IDEA.
2. Select `File > Open`.
3. Choose this folder:

```text
C:\Users\Admin\Documents\Codex\2026-06-01\claude-anthropics-elizaos-agentic-ai-provide\outputs\payment-domain-claude-elizaos-java-agents
```

4. If IntelliJ asks whether to open as a Maven project, choose yes.
5. Make sure the Project SDK is Java 17 or later.

## 2. Create a debug run configuration

1. Open `src/main/java/com/example/payments/Main.java`.
2. Click the green run icon beside `public static void main`.
3. Choose `Modify Run Configuration`.
4. Set:

```text
Main class: com.example.payments.Main
Working directory: C:\Users\Admin\Documents\Codex\2026-06-01\claude-anthropics-elizaos-agentic-ai-provide\outputs\payment-domain-claude-elizaos-java-agents
JDK: Java 17+
```

5. Optional environment variables:

```text
PORT=8080
ANTHROPIC_API_KEY=your_key_here
CLAUDE_MODEL=claude-sonnet-4-20250514
```

If you do not set `ANTHROPIC_API_KEY`, the app still works in local fallback mode.

## 3. Add useful breakpoints

Start with these breakpoints:

```text
src/main/java/com/example/payments/Main.java
- main(...)
- createClaudeClient()

src/main/java/com/example/payments/api/AgentHttpServer.java
- handleAgents(...)
- toAgentRequest(...)

src/main/java/com/example/payments/agents/BasePaymentAgent.java
- askClaude(...)

src/main/java/com/example/payments/tools/PaymentToolbox.java
- the method used by the agent you are testing
```

Then set one breakpoint inside the agent you want to test, inside its `handle(...)` method.

## 4. Start debug mode

Click the bug icon in IntelliJ to start the application in debug mode.

Expected console output:

```text
Payment agent backend running at http://localhost:8080
```

Open this URL to confirm the server is running:

```text
http://localhost:8080/agents
```

Do not open `/agents/{agentId}/invoke` directly in the browser. That URL requires a POST request with JSON.

## 5. Test each agent from PowerShell

Open PowerShell in the project folder:

```powershell
cd C:\Users\Admin\Documents\Codex\2026-06-01\claude-anthropics-elizaos-agentic-ai-provide\outputs\payment-domain-claude-elizaos-java-agents
```

### Payment Orchestration Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/PaymentOrchestrationAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/payment-orchestrator.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/payment-orchestrator/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Checks gateway health, detects primary gateway degradation, proposes backup routing and approval-gated capture.
```

### Fraud Risk Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/FraudRiskAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/fraud-risk.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/fraud-risk/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Detects high amount, new device, elevated velocity, AVS failure, and returns HIGH risk with manual review.
```

### Chargeback Dispute Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/ChargebackDisputeAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/chargeback-dispute.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/chargeback-dispute/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Detects fraud dispute reason code, delivery proof, customer contact, and proposes representment preparation.
```

### Reconciliation Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/ReconciliationAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/reconciliation.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/reconciliation/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Detects ledger mismatch and delayed gateway batch, opens a reconciliation case, and approval-gates adjustments.
```

### Subscription Billing Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/SubscriptionBillingAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/subscription-billing.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/subscription-billing/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Detects past-due renewal and third retry, then recommends escalation before pause or cancellation.
```

### KYC AML Compliance Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/KycAmlComplianceAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/kyc-aml-compliance.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/kyc-aml-compliance/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Detects sanctions/PEP/high-risk-country signals and opens enhanced due diligence review.
```

### Refund Support Agent

Breakpoint:

```text
src/main/java/com/example/payments/agents/RefundSupportAgent.java
handle(...)
```

Command:

```powershell
$body = Get-Content examples/refund-support.request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/agents/refund-support/invoke -Method Post -ContentType "application/json" -Body $body
```

Expected behavior:

```text
Detects refund eligibility and manager approval threshold, then prepares an approval-gated refund case.
```

## 6. What to inspect while debugging

In IntelliJ, inspect these values:

```text
request.message()
request.paymentId()
request.amount()
request.merchantId()
request.customerId()
evidence
reasoning
actions
requiresApproval
```

The most important concept:

```text
Claude can explain and reason.
Java decides what actions are allowed.
Money-moving actions require approval.
```

## 7. Stop debug mode

Click the red stop button in IntelliJ.

If you started another Java process outside IntelliJ, stop it from PowerShell:

```powershell
Get-Process java
Stop-Process -Id <process_id>
```
