package com.example.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(String apiKey, String model) {
    private static final String DEFAULT_MODEL = "claude-sonnet-4-20250514";

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String resolvedModel() {
        return model == null || model.isBlank() ? DEFAULT_MODEL : model;
    }
}
