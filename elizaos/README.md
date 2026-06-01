# ElizaOS Integration Notes

The Java backend is the source of truth for agent execution. These character files describe the same agents for an ElizaOS orchestration layer.

Typical integration pattern:

1. Run the Java service.
2. Register each character in ElizaOS.
3. Add an ElizaOS action that POSTs to `http://localhost:8080/agents/{agentId}/invoke`.
4. Require human approval for any returned action where `requiresApproval=true`.

The character files intentionally avoid secrets and payment credentials.
