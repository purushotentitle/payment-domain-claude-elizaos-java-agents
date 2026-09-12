package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class MockSettlementLedgerAdapter implements SettlementLedgerAdapter {
    @Override
    public Map<String, Object> settlementBreaks(AgentRequest request) {
        Map<String, Object> breaks = new LinkedHashMap<>();
        breaks.put("ledgerMatch", !PaymentTextMatcher.contains(request.message(), "mismatch"));
        breaks.put("gatewayBatchStatus", PaymentTextMatcher.contains(request.message(), "delayed") ? "delayed" : "closed");
        breaks.put("unmatchedCount", PaymentTextMatcher.contains(request.message(), "mismatch") ? 3 : 0);
        breaks.put("nextCutoff", "T+1 18:00");
        return breaks;
    }
}
