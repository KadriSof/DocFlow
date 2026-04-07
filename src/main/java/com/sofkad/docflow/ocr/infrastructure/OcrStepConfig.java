package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.application.OcrService;
import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

@Configuration
public class OcrStepConfig {

    @Bean
    public Step ocrStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        OcrService ocrService,
                        OcrProperties ocrProperties) {
        return new StepBuilder("ocrStep", jobRepository)
                .<OcrRequest, OcrResult>chunk(ocrProperties.getChunkSize(), transactionManager)
                .reader(ocrItemReader())
                .processor(ocrItemProcessor(ocrService, ocrProperties))
                .writer(ocrItemWriter())
                .faultTolerant()
                .skipLimit(ocrProperties.getSkipLimit())
                .skip(IOException.class)
                .skip(IllegalArgumentException.class)
                .retryLimit(ocrProperties.getRetryMaxAttempts())
                .retry(IOException.class)
                .build();
    }

    @Bean
    public OcrItemReader ocrItemReader() {
        return new OcrItemReader();
    }

    private ItemProcessor<OcrRequest, OcrResult> ocrItemProcessor(OcrService ocrService,
                                                                    OcrProperties ocrProperties) {
        return request -> {
            String language = request.language() != null ? request.language() : ocrProperties.getDefaultLanguage();
            OcrRequest normalizedRequest = new OcrRequest(request.image(), request.pageNumber(), language);
            return ocrService.performOcr(normalizedRequest);
        };
    }

    @Bean
    public OcrItemWriter ocrItemWriter() {
        return new OcrItemWriter();
    }
}
