package com.example.payments.agents;

public final class UnknownAgentException extends RuntimeException {
    public UnknownAgentException(String agentId) {
        super("Unknown payment agent: " + agentId);
    }
}
