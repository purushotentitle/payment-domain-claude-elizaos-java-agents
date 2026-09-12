package com.example.payments.agents;

import com.example.payments.approvals.ApprovalService;
import com.example.payments.audit.InMemoryAuditLog;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentDescriptor;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public final class AgentService {
    private final Map<String, PaymentAgent> agentsById;
    private final AgentContext context;
    private final ApprovalService approvalService;
    private final InMemoryAuditLog auditLog;

    public AgentService(
            List<PaymentAgent> agents,
            AgentContext context,
            ApprovalService approvalService,
            InMemoryAuditLog auditLog
    ) {
        this.agentsById = agents.stream()
                .sorted(Comparator.comparing(PaymentAgent::id))
                .collect(Collectors.toMap(
                        PaymentAgent::id,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        this.context = context;
        this.approvalService = approvalService;
        this.auditLog = auditLog;
    }

    public List<AgentDescriptor> listAgents() {
        return agentsById.values().stream()
                .map(agent -> new AgentDescriptor(agent.id(), agent.name(), agent.description()))
                .toList();
    }

    public AgentResponse invoke(String agentId, AgentRequest request) {
        PaymentAgent agent = agentsById.get(agentId);
        if (agent == null) {
            throw new UnknownAgentException(agentId);
        }

        AgentResponse rawResponse = agent.handle(request, context);
        AgentResponse responseWithApprovals = approvalService.createRequiredApprovals(rawResponse, request);

        Map<String, Object> auditDetails = new LinkedHashMap<>();
        auditDetails.put("agentId", agentId);
        auditDetails.put("paymentId", request.paymentId());
        auditDetails.put("merchantId", request.merchantId());
        auditDetails.put("customerId", request.customerId());
        auditDetails.put("riskLevel", responseWithApprovals.riskLevel());
        auditDetails.put("requiresApproval", responseWithApprovals.requiresApproval());
        auditLog.record("AGENT_INVOKED", auditDetails);

        return responseWithApprovals;
    }
}
