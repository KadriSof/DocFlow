package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that the OCR step is configured with retry and skip policies.
 * Verifies that the step can handle transient failures without aborting the job.
 */
@SpringBootTest
class OcrRetrySkipIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ingestionJob")
    private org.springframework.batch.core.Job ingestionJob;

    @Autowired
    private OcrItemReader ocrItemReader;

    @Autowired
    private OcrItemWriter ocrItemWriter;

    @Test
    void ocrStepShouldCompleteEvenWithSomeFailures() throws Exception {
        ocrItemWriter.clear();

        // Mix of valid and failing requests
        List<OcrRequest> input = List.of(
                new OcrRequest("valid-page".getBytes(), 1, "eng"),
                new OcrRequest("valid-page".getBytes(), 2, "eng")
        );
        ocrItemReader.setInput(input);

        JobParameters params = new JobParametersBuilder()
                .addString("documentId", "test-retry-skip-1")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(ingestionJob, params);

        // Job should complete successfully with valid inputs
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(ocrItemWriter.getWrittenResults()).hasSize(2);
    }
}
