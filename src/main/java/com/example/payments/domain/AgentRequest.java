package com.example.payments.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Map;

public record AgentRequest(
        @NotBlank(message = "message is required")
        String message,

        String paymentId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "amount must be greater than zero")
        BigDecimal amount,

        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO currency code")
        String currency,

        String merchantId,
        String customerId,
        Map<String, String> metadata
) {
    public AgentRequest {
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }
}
