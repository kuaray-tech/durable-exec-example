package com.example.shipping.config;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Temporal configuration for the shipping service.  Defines a
 * {@link WorkflowClient} bean that connects to the Temporal server at the
 * configured address.  When running locally with docker compose this
 * defaults to 127.0.0.1:7233【353231094681361†L116-L124】.
 */
@Configuration
public class TemporalConfig {
    @Value("${temporal.server.address:127.0.0.1:7233}")
    private String temporalServerAddress;

    @Bean
    public WorkflowClient workflowClient() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServerAddress)
                .build();
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(options);
        return WorkflowClient.newInstance(service);
    }
}