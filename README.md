# Payment Domain Claude + ElizaOS Java Agents

Java backend examples for agentic AI workflows in the payment domain. The service exposes multiple payment agents over HTTP, calls Claude through Anthropic's Messages API when `ANTHROPIC_API_KEY` is available, and falls back to deterministic local recommendations for development.

## Agents included

- `payment-orchestrator` - authorisation routing, retry strategy, 3DS/manual-review decisions.
- `fraud-risk` - transaction risk scoring and fraud operations guidance.
- `chargeback-dispute` - evidence checklist and representment strategy.
- `reconciliation` - settlement, ledger, and gateway mismatch triage.
- `subscription-billing` - recurring payment failure, dunning, and account-state decisions.
- `kyc-aml-compliance` - sanctions, AML, and enhanced due diligence review.
- `refund-support` - refund eligibility, customer-support next actions, and approval gating.

## Why this structure

The core runtime is Java, while the `elizaos/characters` folder stores ElizaOS-style character definitions so each Java agent can be represented in an ElizaOS orchestration layer. In a production deployment, ElizaOS can call this Java service through HTTP actions or a custom plugin.

## Run the application locally

Requirements:

- Java 17+
- Optional: `ANTHROPIC_API_KEY`
- Optional: Maven, if you prefer Maven builds

For step-by-step IntelliJ debug testing with sample payloads for every agent, see [INTELLIJ_DEBUG_TESTING.md](INTELLIJ_DEBUG_TESTING.md).

### 1. Open the project folder

```powershell
cd C:\Users\Admin\Documents\Codex\2026-06-01\claude-anthropics-elizaos-agentic-ai-provide\outputs\payment-domain-claude-elizaos-java-agents
```

### 2. Compile the Java code

This project has no required external runtime dependencies, so you can compile it directly with the JDK:

```powershell
javac -d build/classes (Get-ChildItem -Recurse src/main/java/*.java).FullName
```

If you use Maven, this also works:

```powershell
mvn package
```

### 3. Optional: enable Claude reasoning

Without an Anthropic key, the app still runs in local fallback mode. To enable Claude reasoning, set:

```powershell
$env:ANTHROPIC_API_KEY="your_key_here"
```

You can also choose a model:

```powershell
$env:CLAUDE_MODEL="claude-sonnet-4-20250514"
```

### 4. Start the backend

Direct JDK run:

```powershell
java -cp build/classes com.example.payments.Main
```

If you built with Maven:

```powershell
java -jar target/payment-domain-claude-elizaos-java-agents-1.0.0.jar
```

The server starts on:

```text
http://localhost:8080
```

To use another port:

```powershell
$env:PORT="9090"
java -cp build/classes com.example.payments.Main
```

### 5. Check that the app is running

Open this in a browser:

```text
http://localhost:8080/agents
```

Or test from PowerShell:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/health -Method Get
Invoke-RestMethod -Uri http://localhost:8080/agents -Method Get
```

Important: URLs like `/agents/fraud-risk/invoke` are POST API endpoints. They are not normal browser pages. Use PowerShell, curl, Postman, ElizaOS, or another HTTP client to call them.

### 6. Invoke a payment agent

Fraud risk example:

```powershell
$body = Get-Content examples/fraud-risk.request.json -Raw

Invoke-RestMethod `
  -Uri http://localhost:8080/agents/fraud-risk/invoke `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Refund support example:

```powershell
$body = Get-Content examples/refund-support.request.json -Raw

Invoke-RestMethod `
  -Uri http://localhost:8080/agents/refund-support/invoke `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

### 7. Stop the application

If the app is running in the current terminal, press:

```text
Ctrl+C
```

If it was started in the background, find and stop the Java process:

```powershell
Get-Process java
Stop-Process -Id <process_id>
```

## API

List agents:

```bash
curl http://localhost:8080/agents
```

Invoke an agent:

```bash
curl -X POST http://localhost:8080/agents/fraud-risk/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Evaluate this card payment before capture",
    "paymentId": "pay_123",
    "amount": 2499.00,
    "currency": "USD",
    "merchantId": "mrc_high_value",
    "customerId": "cust_new_device"
  }'
```

## Safety model

These examples do not move real money. Any action that would capture, refund, block, offboard, or file representment is returned as an `ActionProposal` with `requiresApproval=true`. Use this pattern when connecting agents to real payment gateways.

## Repository layout

```text
src/main/java/com/example/payments
  Main.java                         HTTP bootstrap
  agents/                           Payment-domain agents
  anthropic/                        Claude client
  api/                              HTTP request handling
  domain/                           Shared request/response models
  tools/                            Mock payment tools and data providers
elizaos/characters/                 Character definitions for ElizaOS
examples/                           Example requests
```

## Production notes

- Replace `PaymentToolbox` mocks with gateway, ledger, CRM, KYC, and dispute-provider adapters.
- Store conversation and decision audit trails with immutable IDs.
- Put hard policy checks before model calls for PCI, AML, sanctions, SCA, and refund rules.
- Add human approval queues for high-risk and money-moving proposals.
- Redact PAN, CVV, auth credentials, and sensitive PII before any model request.
