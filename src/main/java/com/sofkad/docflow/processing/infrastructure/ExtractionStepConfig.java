package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.application.DocumentTypeDetector;
import com.sofkad.docflow.processing.application.ExtractionService;
import com.sofkad.docflow.processing.domain.ExtractionRequest;
import com.sofkad.docflow.processing.domain.ExtractionResult;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExtractionStepConfig {

    @Bean
    public Step extractionStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ExtractionService extractionService,
                               ProcessingProperties processingProperties) {
        return new StepBuilder("extractionStep", jobRepository)
                .<ExtractionRequest, ExtractionResult>chunk(processingProperties.getChunkSize(), transactionManager)
                .reader(extractionItemReader())
                .processor(extractionItemProcessor(extractionService))
                .writer(extractionItemWriter())
                .faultTolerant()
                .skipLimit(processingProperties.getSkipLimit())
                .skip(IllegalArgumentException.class)
                .skip(RuntimeException.class)
                .build();
    }

    @Bean
    public ExtractionItemReader extractionItemReader() {
        return new ExtractionItemReader();
    }

    private ItemProcessor<ExtractionRequest, ExtractionResult> extractionItemProcessor(ExtractionService extractionService) {
        return extractionService::extract;
    }

    @Bean
    public ExtractionItemWriter extractionItemWriter() {
        return new ExtractionItemWriter();
    }
}
