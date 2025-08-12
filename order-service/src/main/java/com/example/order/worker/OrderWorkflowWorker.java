package com.example.order.worker;

import com.example.order.workflow.OrderWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * OrderWorkflowWorker spins up a Temporal worker that polls the task queue
 * configured for order workflows and executes {@link OrderWorkflowImpl}
 * instances.  It is created as a Spring singleton and starts when the
 * application context is initialised.  Because a Temporal worker runs
 * indefinitely polling for tasks, this component should be used in
 * conjunction with a REST controller which submits workflows to the queue.
 */
@Component
@RequiredArgsConstructor
public class OrderWorkflowWorker {

    private static final Logger logger = LoggerFactory.getLogger(OrderWorkflowWorker.class);

    private final WorkflowClient workflowClient;

    @Value("${temporal.order.taskQueue:ORDER_TASK_QUEUE}")
    private String orderTaskQueue;

    @PostConstruct
    public void start() {
        // Create a factory for workers bound to this WorkflowClient.  Multiple
        // workers can be created from the same factory to listen on different
        // task queues.
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(orderTaskQueue);
        // Register the workflow implementation type.  Temporal will create a
        // new instance per workflow execution.
        worker.registerWorkflowImplementationTypes(OrderWorkflowImpl.class);
        // Start polling for workflow tasks.  This call returns immediately
        // and runs in background threads.
        factory.start();
        logger.info("Order workflow worker started and polling task queue: {}", orderTaskQueue);
    }
}