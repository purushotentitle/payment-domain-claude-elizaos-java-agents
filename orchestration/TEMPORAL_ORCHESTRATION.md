# Temporal Orchestration

Temporal is the recommended production orchestration layer for this payment-agent backend.

## Why Temporal instead of ElizaOS

ElizaOS is useful for conversational agent identity, but payment workflows need stronger operational guarantees:

- durable workflow state
- retries with backoff
- activity timeouts
- worker-based scaling
- audit-friendly workflow history
- long-running human approval steps
- resilience when a worker or process restarts

For payment systems, the safer architecture is:

```text
Client / Operations UI
  -> Spring Boot REST API
  -> Temporal Workflow
  -> Temporal Activity
  -> Java Payment Agent
  -> Claude reasoning
  -> Approval case
  -> Audit event
```

## What was added

The Java package `com.example.payments.orchestration` contains:

```text
PaymentReviewWorkflow.java
  Temporal workflow interface.

PaymentReviewWorkflowImpl.java
  Durable workflow implementation. It calls the activity with timeout and retry policy.

PaymentAgentActivities.java
  Temporal activity interface.

SpringPaymentAgentActivities.java
  Activity implementation. It delegates to AgentService.

TemporalWorkerLifecycle.java
  Optional Spring lifecycle bean that starts a Temporal worker.

TemporalPaymentReviewLauncher.java
  Optional Spring service that starts a workflow execution.

PaymentReviewStartResponse.java
  API response for workflow-start requests.
```

The endpoint `POST /orchestrations/payment-review/{agentId}` starts a Temporal workflow when the Temporal client is enabled.

## Local REST mode

By default, Temporal is disabled so the app runs without a Temporal server:

```text
TEMPORAL_CLIENT_ENABLED=false
TEMPORAL_WORKER_ENABLED=false
```

Use direct agent invocation for local testing:

```text
POST /agents/{agentId}/invoke
```

## Temporal mode

Run a Temporal service locally, then start the Spring Boot app with:

```powershell
$env:TEMPORAL_CLIENT_ENABLED="true"
$env:TEMPORAL_WORKER_ENABLED="true"
$env:TEMPORAL_TARGET="127.0.0.1:7233"
$env:TEMPORAL_NAMESPACE="default"
$env:TEMPORAL_TASK_QUEUE="payment-agent-task-queue"
mvn spring-boot:run
```

Start a durable workflow:

```powershell
$body = Get-Content examples/fraud-risk.request.json -Raw

Invoke-RestMethod `
  -Uri http://localhost:8080/orchestrations/payment-review/fraud-risk `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

If `PAYMENT_AGENT_API_KEY` is enabled, include:

```powershell
-Headers @{ "X-API-Key" = "local-dev-key" }
```

## Interview explanation

Say this:

```text
I chose Temporal over ElizaOS as the main orchestration layer because payment workflows need durable execution, retries, long-running approvals, and auditability. ElizaOS is better for conversational agent identity, while Temporal is better for production workflow reliability. In my design, Spring Boot exposes APIs, Temporal orchestrates durable workflows, Java agents enforce rules, Claude provides reasoning, and approval/audit services protect money-moving actions.
```
