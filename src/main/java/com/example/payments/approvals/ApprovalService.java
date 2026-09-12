package com.example.payments.approvals;

import com.example.payments.audit.InMemoryAuditLog;
import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public final class ApprovalService {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentHashMap<String, ApprovalCase> approvals = new ConcurrentHashMap<>();
    private final InMemoryAuditLog auditLog;

    public ApprovalService(InMemoryAuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public AgentResponse createRequiredApprovals(AgentResponse response, AgentRequest request) {
        List<ActionProposal> actions = response.actions().stream()
                .map(action -> attachApprovalIfRequired(response.agentId(), action, request))
                .toList();

        return new AgentResponse(
                response.agentId(),
                response.summary(),
                response.recommendation(),
                response.riskLevel(),
                response.requiresApproval(),
                actions,
                response.evidence()
        );
    }

    public ApprovalCase create(CreateApprovalRequest request) {
        ApprovalCase approval = new ApprovalCase(
                "apr_" + sequence.incrementAndGet(),
                request.agentId(),
                request.actionType(),
                request.description(),
                ApprovalStatus.PENDING,
                request.paymentId(),
                request.merchantId(),
                request.customerId(),
                Instant.now(),
                null,
                null,
                null
        );
        approvals.put(approval.id(), approval);
        auditLog.record("APPROVAL_CREATED", auditDetails(approval));
        return approval;
    }

    public List<ApprovalCase> list() {
        return approvals.values().stream()
                .sorted(Comparator.comparing(ApprovalCase::createdAt))
                .toList();
    }

    public Optional<ApprovalCase> find(String approvalId) {
        return Optional.ofNullable(approvals.get(approvalId));
    }

    public ApprovalCase approve(String approvalId, ApprovalDecisionRequest request) {
        return decide(approvalId, ApprovalStatus.APPROVED, request);
    }

    public ApprovalCase reject(String approvalId, ApprovalDecisionRequest request) {
        return decide(approvalId, ApprovalStatus.REJECTED, request);
    }

    private ActionProposal attachApprovalIfRequired(String agentId, ActionProposal action, AgentRequest request) {
        if (!action.requiresApproval() || action.approvalId() != null) {
            return action;
        }

        ApprovalCase approval = create(new CreateApprovalRequest(
                agentId,
                action.type(),
                action.description(),
                request.paymentId(),
                request.merchantId(),
                request.customerId()
        ));
        return action.withApprovalId(approval.id());
    }

    private ApprovalCase decide(String approvalId, ApprovalStatus status, ApprovalDecisionRequest request) {
        ApprovalCase existing = approvals.get(approvalId);
        if (existing == null) {
            throw new ApprovalNotFoundException(approvalId);
        }

        ApprovalCase decided = existing.withDecision(status, request.reviewer(), request.notes());
        approvals.put(approvalId, decided);
        auditLog.record(status == ApprovalStatus.APPROVED ? "APPROVAL_APPROVED" : "APPROVAL_REJECTED", auditDetails(decided));
        return decided;
    }

    private Map<String, Object> auditDetails(ApprovalCase approval) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("approvalId", approval.id());
        details.put("agentId", approval.agentId());
        details.put("actionType", approval.actionType());
        details.put("status", approval.status());
        details.put("paymentId", approval.paymentId());
        details.put("merchantId", approval.merchantId());
        details.put("customerId", approval.customerId());
        details.put("reviewer", approval.reviewer());
        return details;
    }
}
