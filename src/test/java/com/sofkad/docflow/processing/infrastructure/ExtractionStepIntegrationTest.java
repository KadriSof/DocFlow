package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.domain.DocumentType;
import com.sofkad.docflow.processing.domain.ExtractionRequest;
import com.sofkad.docflow.processing.domain.ExtractionResult;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExtractionStepIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ingestionJob")
    private org.springframework.batch.core.Job ingestionJob;

    @Autowired
    private ExtractionItemReader extractionItemReader;

    @Autowired
    private ExtractionItemWriter extractionItemWriter;

    @Test
    void extractionStepShouldProcessMultipleDocuments() throws Exception {
        extractionItemWriter.clear();
        List<ExtractionRequest> input = List.of(
                new ExtractionRequest("invoice total: $150.00", DocumentType.INVOICE),
                new ExtractionRequest("invoice #INV-2024-001 amount due: $250.00", DocumentType.INVOICE)
        );
        extractionItemReader.setInput(input);

        JobParameters params = new JobParametersBuilder()
                .addString("documentId", "test-doc-extract-1")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(ingestionJob, params);

        assertThat(execution.getStatus()).isIn(BatchStatus.COMPLETED, BatchStatus.STARTED);

        List<ExtractionResult> results = extractionItemWriter.getWrittenResults();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).isComplete()).isTrue();
        assertThat(results.get(1).isComplete()).isTrue();
    }
}
