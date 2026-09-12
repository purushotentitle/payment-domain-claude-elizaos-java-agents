# Payment Domain Claude + Temporal Java Agents

Spring Boot Java backend examples for agentic AI workflows in the payment domain. The service exposes multiple payment agents over REST APIs, calls Claude through Anthropic's Messages API when `ANTHROPIC_API_KEY` is available, and falls back to deterministic local recommendations for development.

## Agents included

- `payment-orchestrator` - authorisation routing, retry strategy, 3DS/manual-review decisions.
- `fraud-risk` - transaction risk scoring and fraud operations guidance.
- `chargeback-dispute` - evidence checklist and representment strategy.
- `reconciliation` - settlement, ledger, and gateway mismatch triage.
- `subscription-billing` - recurring payment failure, dunning, and account-state decisions.
- `kyc-aml-compliance` - sanctions, AML, and enhanced due diligence review.
- `refund-support` - refund eligibility, customer-support next actions, and approval gating.

## Why this structure

The core runtime is Java/Spring Boot. Temporal is the recommended orchestration layer because payment workflows need durable state, retries, timeouts, long-running human approvals, and auditable workflow history.

ElizaOS files remain in `elizaos/` as optional conversational-agent examples, but they are no longer the preferred orchestration strategy for production payment flows.

For the Temporal workflow design, see [orchestration/TEMPORAL_ORCHESTRATION.md](orchestration/TEMPORAL_ORCHESTRATION.md).

## Run the application locally

Requirements:

- Java 17+
- Maven 3.9+
- Optional: `ANTHROPIC_API_KEY`, for Claude reasoning
- Optional: `PAYMENT_AGENT_API_KEY`, for local API-key protection
- Optional: Temporal service, if you want durable workflow orchestration

For step-by-step IntelliJ debug testing with sample payloads for every agent, see [INTELLIJ_DEBUG_TESTING.md](INTELLIJ_DEBUG_TESTING.md).

### 1. Open the project folder

```powershell
cd C:\Users\Admin\Documents\Codex\2026-06-01\claude-anthropics-elizaos-agentic-ai-provide\outputs\payment-domain-claude-elizaos-java-agents
```

### 2. Build and test

Run the full test suite:

```powershell
mvn test
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

Use Spring Boot:

```powershell
mvn spring-boot:run
```

Or build and run the jar:

```powershell
mvn package
java -jar target/payment-domain-claude-temporal-java-agents-1.0.0.jar
```

The server starts on:

```text
http://localhost:8080
```

To use another port:

```powershell
$env:PORT="9090"
mvn spring-boot:run
```

To protect the agent, approval, and audit APIs with a local API key:

```powershell
$env:PAYMENT_AGENT_API_KEY="local-dev-key"
mvn spring-boot:run
```

### 5. Check that the app is running

Open this in a browser:

```text
http://localhost:8080/agents
```

Or test from PowerShell:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/health -Method Get
Invoke-RestMethod -Uri http://localhost:8080/actuator/health -Method Get
Invoke-RestMethod -Uri http://localhost:8080/agents -Method Get
```

Important: URLs like `/agents/fraud-risk/invoke` are POST API endpoints. They are not normal browser pages. Use PowerShell, curl, Postman, ElizaOS, or another HTTP client to call them.

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

Temporal workflow start endpoint:

```text
POST http://localhost:8080/orchestrations/payment-review/{agentId}
```

By default this returns `501 NOT_IMPLEMENTED` because Temporal is disabled for local simplicity. Enable it with:

```powershell
$env:TEMPORAL_CLIENT_ENABLED="true"
$env:TEMPORAL_WORKER_ENABLED="true"
```

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

If `PAYMENT_AGENT_API_KEY` is set:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/agents/fraud-risk/invoke `
  -Method Post `
  -ContentType "application/json" `
  -Headers @{ "X-API-Key" = "local-dev-key" } `
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

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

List agents:

```bash
curl http://localhost:8080/agents
```

List approval cases:

```bash
curl http://localhost:8080/approvals
```

Approve a pending case:

```bash
curl -X POST http://localhost:8080/approvals/apr_1/approve \
  -H "Content-Type: application/json" \
  -d '{"reviewer":"ops_lead","notes":"Approved after review"}'
```

List audit events:

```bash
curl http://localhost:8080/audit
```

Start a Temporal payment-review workflow:

```bash
curl -X POST http://localhost:8080/orchestrations/payment-review/fraud-risk \
  -H "Content-Type: application/json" \
  -d @examples/fraud-risk.request.json
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

These examples do not move real money. Any action that would capture, refund, block, offboard, post an accounting adjustment, or file representment is returned as an `ActionProposal` with `requiresApproval=true`.

When an agent returns an approval-gated action, the Spring service creates a pending `ApprovalCase` and attaches its `approvalId` to the action. Reviewers can approve or reject these cases through `/approvals/{approvalId}/approve` and `/approvals/{approvalId}/reject`.

## Repository layout

```text
src/main/java/com/example/payments
  Main.java                         Spring Boot bootstrap
  agents/                           Payment-domain agents
  adapters/                         Mock gateway, ledger, risk, KYC, dispute, billing, and refund adapters
  anthropic/                        Claude client
  api/                              Spring REST controllers and exception handling
  approvals/                        Approval workflow
  audit/                            In-memory audit log
  config/                           Spring configuration
  domain/                           Shared request/response models
  orchestration/                    Temporal workflow, activity, worker, and launcher code
  security/                         API key guard and sensitive-data redaction
  tools/                            Payment toolbox facade over adapters
orchestration/                      Temporal design documentation
elizaos/                            Optional legacy conversational-agent examples
examples/                           Example requests
```

## Production notes

- Replace mock adapters with gateway, ledger, CRM, KYC, and dispute-provider integrations.
- Store conversation and decision audit trails with immutable IDs.
- Put hard policy checks before model calls for PCI, AML, sanctions, SCA, and refund rules.
- Persist approval cases in a database and integrate with a reviewer queue.
- Redact PAN, CVV, auth credentials, and sensitive PII before any model request.
- Add OAuth/JWT or service-to-service authentication before exposing outside local development.
