package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OcrStepIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ingestionJob")
    private Job ingestionJob;

    @Autowired
    private OcrItemReader ocrItemReader;

    @Autowired
    private OcrItemWriter ocrItemWriter;

    @Test
    void ocrStepShouldProcessMultiplePages() throws Exception {
        ocrItemWriter.clear();
        List<OcrRequest> input = List.of(
                new OcrRequest("page1-image".getBytes(), 1, "eng"),
                new OcrRequest("page2-image".getBytes(), 2, "eng"),
                new OcrRequest("page3-image".getBytes(), 3, "eng")
        );
        ocrItemReader.setInput(input);

        JobParameters params = new JobParametersBuilder()
                .addString("documentId", "test-doc-ocr-1")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(ingestionJob, params);

        assertThat(execution.getStatus()).isIn(BatchStatus.COMPLETED, BatchStatus.STARTED);

        List<OcrResult> results = ocrItemWriter.getWrittenResults();
        assertThat(results).hasSize(3);
        assertThat(results.get(0).text()).contains("page 1");
        assertThat(results.get(1).text()).contains("page 2");
        assertThat(results.get(2).text()).contains("page 3");
    }
}
