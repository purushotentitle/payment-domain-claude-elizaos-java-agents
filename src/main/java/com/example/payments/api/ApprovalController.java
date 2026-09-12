package com.example.payments.api;

import com.example.payments.approvals.ApprovalCase;
import com.example.payments.approvals.ApprovalDecisionRequest;
import com.example.payments.approvals.ApprovalNotFoundException;
import com.example.payments.approvals.ApprovalService;
import com.example.payments.approvals.CreateApprovalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/approvals")
@Tag(name = "Approvals", description = "Human approval workflow for sensitive payment actions")
public class ApprovalController {
    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    @Operation(summary = "List approval cases")
    public List<ApprovalCase> listApprovals() {
        return approvalService.list();
    }

    @GetMapping("/{approvalId}")
    @Operation(summary = "Get approval case by ID")
    public ApprovalCase getApproval(@PathVariable String approvalId) {
        return approvalService.find(approvalId).orElseThrow(() -> new ApprovalNotFoundException(approvalId));
    }

    @PostMapping
    @Operation(summary = "Create an approval case manually")
    public ApprovalCase createApproval(@Valid @RequestBody CreateApprovalRequest request) {
        return approvalService.create(request);
    }

    @PostMapping("/{approvalId}/approve")
    @Operation(summary = "Approve a pending action")
    public ApprovalCase approve(
            @PathVariable String approvalId,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        return approvalService.approve(approvalId, request);
    }

    @PostMapping("/{approvalId}/reject")
    @Operation(summary = "Reject a pending action")
    public ApprovalCase reject(
            @PathVariable String approvalId,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        return approvalService.reject(approvalId, request);
    }
}
