package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Spring Batch ItemReader that loads a PDF from the file storage service
 * and delegates to PdfPageItemReader to read pages one at a time.
 */
public class DocumentOcrItemReader implements ItemReader<OcrRequest> {

    private static final Logger log = LoggerFactory.getLogger(DocumentOcrItemReader.class);

    private final FileStorageService fileStorageService;
    private final String defaultLanguage;
    private final PdfPageItemReader delegate;
    private boolean initialized = false;

    public DocumentOcrItemReader(FileStorageService fileStorageService, String defaultLanguage) {
        this.fileStorageService = fileStorageService;
        this.defaultLanguage = defaultLanguage;
        this.delegate = new PdfPageItemReader(defaultLanguage);
    }

    private UUID documentId;

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
        this.initialized = false;
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
            } catch (IllegalArgumentException e) {
                log.error("PDF file not found for document {}: {}", documentId, e.getMessage());
                return null;
            }
        }

        return delegate.read();
    }

    public void close() throws Exception {
        delegate.close();
    }
}
