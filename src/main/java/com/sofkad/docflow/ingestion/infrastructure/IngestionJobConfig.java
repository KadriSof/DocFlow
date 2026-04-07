package com.sofkad.docflow.ingestion.infrastructure;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IngestionJobConfig {

    @Bean
    public Job ingestionJob(JobRepository jobRepository, Step dummyStep, Step ocrStep, Step extractionStep) {
        return new JobBuilder("ingestionJob", jobRepository)
                .start(dummyStep)
                .next(ocrStep)
                .next(extractionStep)
                .build();
    }

    @Bean
    public Step dummyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        Tasklet noOp = (contribution, chunkContext) -> null;
        return new org.springframework.batch.core.step.builder.StepBuilder("dummyStep", jobRepository)
                .tasklet(noOp, transactionManager)
                .build();
    }
}
