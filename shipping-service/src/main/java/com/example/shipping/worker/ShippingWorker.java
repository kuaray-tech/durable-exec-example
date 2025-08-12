package com.example.shipping.worker;

import com.example.shipping.activity.ShippingActivityImpl;
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
 * Worker that executes shipping Activities.  It polls the task queue
 * configured for shipping Activities and registers the
 * {@link ShippingActivityImpl} implementation.  The worker starts
 * automatically when the application context is created.
 */
@Component
@RequiredArgsConstructor
public class ShippingWorker {
    private static final Logger logger = LoggerFactory.getLogger(ShippingWorker.class);
    private final WorkflowClient workflowClient;
    private final ShippingActivityImpl shippingActivity;

    @Value("${temporal.shipping.taskQueue:SHIPPING_ACTIVITY_TASK_QUEUE}")
    private String shippingTaskQueue;

    @PostConstruct
    public void start() {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(shippingTaskQueue);
        worker.registerActivitiesImplementations(shippingActivity);
        factory.start();
        logger.info("Shipping worker started and polling task queue: {}", shippingTaskQueue);
    }
}