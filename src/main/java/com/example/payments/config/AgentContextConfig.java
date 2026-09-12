package com.example.payments.config;

import com.example.payments.anthropic.ClaudeClient;
import com.example.payments.domain.AgentContext;
import com.example.payments.security.SensitiveDataRedactor;
import com.example.payments.tools.PaymentToolbox;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentContextConfig {
    @Bean
    AgentContext agentContext(
            ClaudeClient claudeClient,
            PaymentToolbox paymentToolbox,
            SensitiveDataRedactor redactor
    ) {
        return new AgentContext(claudeClient, paymentToolbox, redactor);
    }
}
