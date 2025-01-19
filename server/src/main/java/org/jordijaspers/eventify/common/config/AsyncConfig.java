package org.jordijaspers.eventify.common.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * A default configuration for asynchronous processing.
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    /**
     * Creates an executor for the event storage.
     *
     * @return the executor
     */
    @Bean
    public Executor eventStorageExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("EventStorage-");
        executor.initialize();
        return executor;
    }
}
