package com.example.payments.adapters;

import com.example.payments.domain.AgentRequest;

import java.util.Map;

public interface SettlementLedgerAdapter {
    Map<String, Object> settlementBreaks(AgentRequest request);
}
