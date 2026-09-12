package com.example.payments.security;

import com.example.payments.domain.AgentRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public final class SensitiveDataRedactor {
    private static final Pattern CARD_NUMBER = Pattern.compile("\\b\\d{13,19}\\b");

    public AgentRequest redact(AgentRequest request) {
        if (request == null) {
            return null;
        }

        Map<String, String> redactedMetadata = new LinkedHashMap<>();
        request.metadata().forEach((key, value) -> {
            if (isSensitiveKey(key)) {
                redactedMetadata.put(key, "[REDACTED]");
            } else {
                redactedMetadata.put(key, redactText(value));
            }
        });

        return new AgentRequest(
                redactText(request.message()),
                request.paymentId(),
                request.amount(),
                request.currency(),
                request.merchantId(),
                request.customerId(),
                redactedMetadata
        );
    }

    public String redactText(String value) {
        if (value == null) {
            return null;
        }
        return CARD_NUMBER.matcher(value).replaceAll("[REDACTED_CARD_NUMBER]");
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("pan")
                || normalized.contains("card")
                || normalized.contains("cvv")
                || normalized.contains("cvc")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("password");
    }
}
