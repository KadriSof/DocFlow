package com.sofkad.docflow;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.infrastructure.OcrItemReader;
import com.sofkad.docflow.ocr.infrastructure.OcrItemWriter;
import com.sofkad.docflow.processing.domain.DocumentType;
import com.sofkad.docflow.processing.domain.ExtractionRequest;
import com.sofkad.docflow.processing.infrastructure.ExtractionItemReader;
import com.sofkad.docflow.processing.infrastructure.ExtractionItemWriter;
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

/**
 * Full pipeline integration test verifying the complete ingestion pipeline:
 * Upload → OCR → Extraction
 */
@SpringBootTest
class FullPipelineIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ingestionJob")
    private org.springframework.batch.core.Job ingestionJob;

    @Autowired
    private OcrItemReader ocrItemReader;

    @Autowired
    private OcrItemWriter ocrItemWriter;

    @Autowired
    private ExtractionItemReader extractionItemReader;

    @Autowired
    private ExtractionItemWriter extractionItemWriter;

    @Test
    void fullPipelineShouldProcessDocumentEndToEnd() throws Exception {
        // Simulate OCR input: 2 pages from a processed PDF
        ocrItemReader.setInput(List.of(
                new OcrRequest("page 1 content".getBytes(), 1, "eng"),
                new OcrRequest("page 2 content".getBytes(), 2, "eng")
        ));

        // Simulate extraction input: OCR results as text
        extractionItemReader.setInput(List.of(
                new ExtractionRequest("INVOICE #INV-2024-001 total amount due: $500.00", DocumentType.INVOICE)
        ));

        JobParameters params = new JobParametersBuilder()
                .addString("documentId", "test-full-pipeline-1")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(ingestionJob, params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions()).hasSize(3);
        assertThat(execution.getStepExecutions())
                .extracting(se -> se.getStepName())
                .containsExactlyInAnyOrder("dummyStep", "ocrStep", "extractionStep");

        // Verify OCR step produced results
        assertThat(ocrItemWriter.getWrittenResults()).hasSize(2);

        // Verify extraction step produced results
        assertThat(extractionItemWriter.getWrittenResults()).hasSize(1);
        assertThat(extractionItemWriter.getWrittenResults().get(0).isComplete()).isTrue();
    }
}
