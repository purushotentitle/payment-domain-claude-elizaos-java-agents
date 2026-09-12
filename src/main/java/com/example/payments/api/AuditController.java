package com.example.payments.api;

import com.example.payments.audit.AuditEvent;
import com.example.payments.audit.InMemoryAuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@Tag(name = "Audit", description = "Read in-memory audit events for demo and testing")
public class AuditController {
    private final InMemoryAuditLog auditLog;

    public AuditController(InMemoryAuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @GetMapping
    @Operation(summary = "List audit events")
    public List<AuditEvent> listEvents() {
        return auditLog.list();
    }
}
