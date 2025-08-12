package com.example.order.config;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that provides a {@link WorkflowClient} bean for connecting to
 * the Temporal server.  The server address is configurable via the
 * application property {@code temporal.server.address}.  When running
 * locally using the Temporal docker compose environment the default
 * address is "127.0.0.1:7233" as documented in Temporal's self‑hosting
 * guide【353231094681361†L116-L124】.
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