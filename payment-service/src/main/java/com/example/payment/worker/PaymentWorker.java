package com.example.payment.worker;

import com.example.payment.activity.PaymentActivityImpl;
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
 * Creates a Temporal worker for the payment service.  The worker polls
 * the payment task queue for activities and executes the
 * {@link PaymentActivityImpl}.  This component starts automatically when
 * the Spring application context is initialised.
 */
@Component
@RequiredArgsConstructor
public class PaymentWorker {

    private static final Logger logger = LoggerFactory.getLogger(PaymentWorker.class);

    private final WorkflowClient workflowClient;
    private final PaymentActivityImpl paymentActivity;

    /**
     * The name of the task queue that the payment worker listens on.  This
     * value must match the queue used by the workflow when creating the
     * Activity stub.  Defaults to PAYMENT_ACTIVITY_TASK_QUEUE.
     */
    @Value("${temporal.payment.taskQueue:PAYMENT_ACTIVITY_TASK_QUEUE}")
    private String paymentTaskQueue;

    @PostConstruct
    public void start() {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(paymentTaskQueue);
        worker.registerActivitiesImplementations(paymentActivity);
        factory.start();
        logger.info("Payment worker started and polling task queue: {}", paymentTaskQueue);
    }
}