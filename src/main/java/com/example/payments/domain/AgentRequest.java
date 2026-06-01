package com.example.payments.domain;

import java.math.BigDecimal;
import java.util.Map;

public record AgentRequest(
        String message,
        String paymentId,
        BigDecimal amount,
        String currency,
        String merchantId,
        String customerId,
        Map<String, String> metadata
) {
}
