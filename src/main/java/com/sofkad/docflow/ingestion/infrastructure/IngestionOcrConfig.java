package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ingestion.domain.DocumentRepository;
import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Ingestion module configuration that provides document-aware OCR reader and writer
 * as primary beans, overriding the default list-based implementations from the OCR module.
 */
@Configuration
public class IngestionOcrConfig {

    @Bean
    @Primary
    public ItemReader<OcrRequest> documentOcrItemReader(@Value("${docflow.ocr.default-language:ara}") String defaultLanguage,
                                                         FileStorageService fileStorageService,
                                                         ItemReader<OcrRequest> defaultOcrItemReader) {
        DocumentOcrItemReader documentReader = new DocumentOcrItemReader(fileStorageService, defaultLanguage);
        return new DelegatingOcrItemReader(documentReader, defaultOcrItemReader);
    }

    @Bean
    @Primary
    public ItemWriter<OcrResult> documentOcrItemWriter(DocumentRepository documentRepository) {
        OcrResultAccumulatingWriter writer = new OcrResultAccumulatingWriter();
        return new DocumentAwareOcrItemWriter(writer, documentRepository);
    }
}
