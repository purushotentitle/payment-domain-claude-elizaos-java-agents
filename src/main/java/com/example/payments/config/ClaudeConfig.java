package com.example.payments.config;

import com.example.payments.anthropic.AnthropicClaudeClient;
import com.example.payments.anthropic.ClaudeClient;
import com.example.payments.anthropic.LocalHeuristicClaudeClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaudeConfig {
    @Bean
    ClaudeClient claudeClient(AnthropicProperties properties, ObjectMapper objectMapper) {
        if (!properties.hasApiKey()) {
            return new LocalHeuristicClaudeClient();
        }
        return new AnthropicClaudeClient(properties.apiKey(), properties.resolvedModel(), objectMapper);
    }
}
