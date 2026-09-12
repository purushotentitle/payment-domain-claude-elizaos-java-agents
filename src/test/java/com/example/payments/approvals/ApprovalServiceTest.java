package com.example.payments.approvals;

import com.example.payments.audit.InMemoryAuditLog;
import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalServiceTest {
    private final InMemoryAuditLog auditLog = new InMemoryAuditLog();
    private final ApprovalService approvalService = new ApprovalService(auditLog);

    @Test
    void createsApprovalCasesForApprovalGatedActions() {
        AgentResponse response = new AgentResponse(
                "fraud-risk",
                "summary",
                "recommendation",
                "HIGH",
                true,
                List.of(new ActionProposal("HOLD_CAPTURE", "Hold capture for review.", true)),
                Map.of()
        );

        AgentResponse updated = approvalService.createRequiredApprovals(response, request());

        assertThat(updated.actions().get(0).approvalId()).startsWith("apr_");
        assertThat(approvalService.list()).hasSize(1);
        assertThat(auditLog.list()).anySatisfy(event -> assertThat(event.type()).isEqualTo("APPROVAL_CREATED"));
    }

    @Test
    void approvesPendingCase() {
        ApprovalCase created = approvalService.create(new CreateApprovalRequest(
                "refund-support",
                "ISSUE_REFUND",
                "Issue refund after policy review.",
                "pay_123",
                "mrc_demo",
                "cust_demo"
        ));

        ApprovalCase approved = approvalService.approve(created.id(), new ApprovalDecisionRequest("ops_lead", "Approved after review."));

        assertThat(approved.status()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(approved.reviewer()).isEqualTo("ops_lead");
        assertThat(approved.decidedAt()).isNotNull();
    }

    private AgentRequest request() {
        return new AgentRequest(
                "Review payment.",
                "pay_123",
                BigDecimal.valueOf(100),
                "USD",
                "mrc_demo",
                "cust_demo",
                Map.of()
        );
    }
}
