package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ingestion.domain.Document;
import com.sofkad.docflow.ingestion.domain.DocumentRepository;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

import java.util.UUID;

/**
 * ItemWriter that delegates to OcrResultAccumulatingWriter and updates the Document entity
 * after the step completes via StepExecutionListener.
 */
public class DocumentAwareOcrItemWriter implements ItemWriter<OcrResult>, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentAwareOcrItemWriter.class);

    private final OcrResultAccumulatingWriter delegate;
    private final DocumentRepository documentRepository;
    private StepExecution stepExecution;

    public DocumentAwareOcrItemWriter(OcrResultAccumulatingWriter delegate,
                                      DocumentRepository documentRepository) {
        this.delegate = delegate;
        this.documentRepository = documentRepository;
    }

    @Override
    public void write(Chunk<? extends OcrResult> chunk) throws Exception {
        delegate.write(chunk);
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public org.springframework.batch.core.ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        String documentIdStr = stepExecution.getJobParameters().getString("documentId");
        if (documentIdStr == null || documentIdStr.isEmpty()) {
            log.debug("No documentId in job parameters, skipping document update");
            return stepExecution.getExitStatus();
        }

        UUID documentId;
        try {
            documentId = UUID.fromString(documentIdStr);
        } catch (IllegalArgumentException e) {
            log.debug("documentId '{}' is not a valid UUID — skipping document update", documentIdStr);
            return stepExecution.getExitStatus();
        }

        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            log.warn("Document {} not found for OCR result update", documentId);
            return stepExecution.getExitStatus();
        }

        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
            String combinedText = delegate.getCombinedText();
            document.markCompleted(combinedText);
            documentRepository.save(document);
            log.info("Document {} marked as completed with {} characters of OCR text",
                    documentId, combinedText.length());
        } else if (stepExecution.getStatus() == BatchStatus.FAILED) {
            String errorMessage = stepExecution.getFailureExceptions().stream()
                    .findFirst()
                    .map(Throwable::getMessage)
                    .orElse("Unknown OCR error");
            document.markFailed(errorMessage);
            documentRepository.save(document);
            log.error("Document {} marked as failed: {}", documentId, errorMessage);
        }

        delegate.clear();
        return stepExecution.getExitStatus();
    }
}
