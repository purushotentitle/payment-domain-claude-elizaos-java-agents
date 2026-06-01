package com.example.payments.anthropic;

public final class LocalHeuristicClaudeClient implements ClaudeClient {
    @Override
    public String complete(String systemPrompt, String userPrompt) {
        return "Local heuristic mode: no ANTHROPIC_API_KEY was provided. "
                + "Use the structured agent response and attached tool evidence as the decision record. "
                + "Set ANTHROPIC_API_KEY to add Claude reasoning.";
    }
}
