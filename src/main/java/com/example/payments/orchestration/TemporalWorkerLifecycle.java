package com.example.payments.orchestration;

import com.example.payments.config.TemporalProperties;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "temporal.worker.enabled", havingValue = "true")
public final class TemporalWorkerLifecycle implements SmartLifecycle {
    private final TemporalProperties properties;
    private final SpringPaymentAgentActivities activities;
    private WorkflowServiceStubs serviceStubs;
    private WorkerFactory workerFactory;
    private boolean running;

    public TemporalWorkerLifecycle(TemporalProperties properties, SpringPaymentAgentActivities activities) {
        this.properties = properties;
        this.activities = activities;
    }

    @Override
    public void start() {
        serviceStubs = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(properties.resolvedTarget())
                        .build()
        );
        WorkflowClient workflowClient = WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(properties.resolvedNamespace())
                        .build()
        );
        workerFactory = WorkerFactory.newInstance(workflowClient);
        Worker worker = workerFactory.newWorker(properties.resolvedTaskQueue());
        worker.registerWorkflowImplementationTypes(PaymentReviewWorkflowImpl.class);
        worker.registerActivitiesImplementations(activities);
        workerFactory.start();
        running = true;
    }

    @Override
    public void stop() {
        if (workerFactory != null) {
            workerFactory.shutdown();
        }
        if (serviceStubs != null) {
            serviceStubs.shutdown();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
