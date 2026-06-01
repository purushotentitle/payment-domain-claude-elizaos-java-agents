package com.example.payments.anthropic;

import java.io.IOException;

public interface ClaudeClient {
    String complete(String systemPrompt, String userPrompt) throws IOException, InterruptedException;
}
