package com.sofkad.docflow.ingestion.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IngestionJobIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ingestionJob")
    private Job ingestionJob;

    @Test
    void ingestionJobShouldLaunchSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("documentId", "test-doc-1")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(ingestionJob, params);

        assertThat(execution.getStatus()).isIn(BatchStatus.COMPLETED, BatchStatus.STARTED);
    }
}
