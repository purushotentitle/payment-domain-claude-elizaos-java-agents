package com.example.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "temporal")
public record TemporalProperties(
        String target,
        String namespace,
        String taskQueue,
        Worker worker,
        Client client
) {
    private static final String DEFAULT_TARGET = "127.0.0.1:7233";
    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_TASK_QUEUE = "payment-agent-task-queue";

    public String resolvedTarget() {
        return target == null || target.isBlank() ? DEFAULT_TARGET : target;
    }

    public String resolvedNamespace() {
        return namespace == null || namespace.isBlank() ? DEFAULT_NAMESPACE : namespace;
    }

    public String resolvedTaskQueue() {
        return taskQueue == null || taskQueue.isBlank() ? DEFAULT_TASK_QUEUE : taskQueue;
    }

    public boolean workerEnabled() {
        return worker != null && worker.enabled();
    }

    public boolean clientEnabled() {
        return client != null && client.enabled();
    }

    public record Worker(boolean enabled) {
    }

    public record Client(boolean enabled) {
    }
}
