package com.example.payments.domain;

import java.util.List;
import java.util.Map;

public record AgentResponse(
        String agentId,
        String summary,
        String recommendation,
        String riskLevel,
        boolean requiresApproval,
        List<ActionProposal> actions,
        Map<String, Object> evidence
) {
}
