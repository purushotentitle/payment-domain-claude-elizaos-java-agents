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

## Run locally

Requirements:

- Java 17+
- Optional: `ANTHROPIC_API_KEY`

Compile:

```powershell
javac -d build/classes (Get-ChildItem -Recurse src/main/java/*.java).FullName
```

Run:

```powershell
$env:ANTHROPIC_API_KEY="your_key_here"
java -cp build/classes com.example.payments.Main
```

The server starts on `http://localhost:8080`. Override with `PORT`.

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
