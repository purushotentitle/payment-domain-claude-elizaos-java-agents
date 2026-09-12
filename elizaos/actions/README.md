# ElizaOS Action Example

`java-payment-agent-action.ts` shows how an ElizaOS action can call the Java Spring Boot backend.

Expected flow:

```text
ElizaOS message
  -> CALL_JAVA_PAYMENT_AGENT action
  -> POST http://localhost:8080/agents/{agentId}/invoke
  -> Java backend returns AgentResponse
  -> ElizaOS sends a concise response back to the user
```

If `PAYMENT_AGENT_API_KEY` is set in the Java app, set the same value as `JAVA_PAYMENT_AGENT_API_KEY` in the ElizaOS runtime and pass it as `X-API-Key`.
