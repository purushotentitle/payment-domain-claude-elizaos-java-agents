# ElizaOS Integration Notes

The Java backend is the source of truth for agent execution. The character files describe the same agents for an ElizaOS orchestration layer, and the `actions` folder contains a TypeScript action example that calls the Spring Boot service.

Typical integration pattern:

1. Run the Java service.
2. Register each character in ElizaOS.
3. Register or adapt `actions/java-payment-agent-action.ts`.
4. Let the action POST to `http://localhost:8080/agents/{agentId}/invoke`.
5. Require human approval for any returned action where `requiresApproval=true`.

The character files intentionally avoid secrets and payment credentials.

If the Java app runs with `PAYMENT_AGENT_API_KEY`, configure the same secret as `JAVA_PAYMENT_AGENT_API_KEY` in the ElizaOS runtime.
