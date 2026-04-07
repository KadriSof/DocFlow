package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

/**
 * Delegating reader that first tries to read from a stored PDF file (via DocumentOcrItemReader).
 * If that returns null (no document ID or file not found), falls back to a secondary reader.
 * This allows the same step to work for both real uploads and test scenarios.
 */
public class DelegatingOcrItemReader implements ItemReader<OcrRequest>, StepExecutionListener {

    private final DocumentOcrItemReader documentReader;
    private final ItemReader<OcrRequest> fallbackReader;
    private boolean usingDocumentReader = true;

    public DelegatingOcrItemReader(DocumentOcrItemReader documentReader,
                                    ItemReader<OcrRequest> fallbackReader) {
        this.documentReader = documentReader;
        this.fallbackReader = fallbackReader;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        documentReader.beforeStep(stepExecution);
    }

    @Override
    public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
        return documentReader.afterStep(stepExecution);
    }

    @Override
    public OcrRequest read() throws Exception {
        if (usingDocumentReader) {
            OcrRequest request = documentReader.read();
            if (request != null) {
                return request;
            }
            // Document reader returned null — try fallback
            usingDocumentReader = false;
        }
        return fallbackReader.read();
    }
}
