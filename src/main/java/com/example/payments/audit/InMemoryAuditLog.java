package com.example.payments.audit;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public final class InMemoryAuditLog {
    private final AtomicLong sequence = new AtomicLong();
    private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();

    public AuditEvent record(String type, Map<String, Object> details) {
        AuditEvent event = new AuditEvent(
                "audit_" + sequence.incrementAndGet(),
                Instant.now(),
                type,
                details == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(details))
        );
        events.add(event);
        return event;
    }

    public List<AuditEvent> list() {
        return new ArrayList<>(events);
    }
}
