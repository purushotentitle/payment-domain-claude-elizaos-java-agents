package com.example.payments.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
        String id,
        Instant timestamp,
        String type,
        Map<String, Object> details
) {
}
