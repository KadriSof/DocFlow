package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Spring Batch ItemReader that loads a PDF from the file storage service
 * and delegates to PdfPageItemReader to read pages one at a time.
 * Implements StepExecutionListener to get documentId from job parameters.
 */
public class DocumentOcrItemReader implements ItemReader<OcrRequest>, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentOcrItemReader.class);

    private final FileStorageService fileStorageService;
    private final String defaultLanguage;
    private final PdfPageItemReader delegate;
    private boolean initialized = false;
    private UUID documentId;

    public DocumentOcrItemReader(FileStorageService fileStorageService, String defaultLanguage) {
        this.fileStorageService = fileStorageService;
        this.defaultLanguage = defaultLanguage;
        this.delegate = new PdfPageItemReader(defaultLanguage);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String docIdStr = stepExecution.getJobParameters().getString("documentId");
        if (docIdStr != null) {
            try {
                this.documentId = UUID.fromString(docIdStr);
                log.info("DocumentOcrItemReader initialized for document {}", documentId);
            } catch (IllegalArgumentException e) {
                log.warn("documentId '{}' is not a valid UUID — will use fallback reader", docIdStr);
                this.documentId = null;
            }
        } else {
            log.warn("No documentId found in job parameters");
        }
        this.initialized = false;
    }

    @Override
    public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
        try {
            delegate.close();
        } catch (Exception e) {
            log.warn("Failed to close PdfPageItemReader", e);
        }
        return stepExecution.getExitStatus();
    }

    @Override
    public OcrRequest read() throws Exception {
        if (documentId == null) {
            log.warn("No document ID set for OCR reader");
            return null;
        }

        if (!initialized) {
            try {
                Path pdfPath = fileStorageService.loadPdf(documentId);
                delegate.setPdfFile(pdfPath.toFile());
                initialized = true;
                log.info("Loaded PDF with {} pages", delegate.getNumberOfPages());
            } catch (Exception e) {
                log.error("Failed to load PDF for document {}: {}", documentId, e.getMessage());
                return null;
            }
        }

        return delegate.read();
    }
}
