package com.example.payments.domain;

import com.example.payments.anthropic.ClaudeClient;
import com.example.payments.tools.PaymentToolbox;

public record AgentContext(ClaudeClient claudeClient, PaymentToolbox paymentToolbox) {
}
