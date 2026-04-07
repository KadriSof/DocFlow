package com.sofkad.docflow.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Configures virtual threads for batch job processing.
 * Spring Boot's auto-configured JobLauncher will use this TaskExecutor.
 */
@Configuration
public class BatchVirtualThreadsConfig {

    @Bean
    @ConditionalOnMissingBean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("batch-");
        executor.setVirtualThreads(true);
        return executor;
    }
}
