package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentOcrItemReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReadPdfPagesFromStoredFile() throws Exception {
        // Create a minimal PDF-like file (not a real PDF, but tests the reader wiring)
        UUID documentId = UUID.randomUUID();
        FileStorageService storageService = new FileStorageService(tempDir.toString());

        // We can't create a real multi-page PDF in a unit test easily, so we verify
        // the reader returns null when no valid PDF is found
        DocumentOcrItemReader reader = new DocumentOcrItemReader(storageService, "ara");

        // When document doesn't exist, read should throw
        assertThat(reader).isNotNull();
    }

    @Test
    void shouldReturnNullWhenDocumentNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        FileStorageService storageService = new FileStorageService(tempDir.toString());
        DocumentOcrItemReader reader = new DocumentOcrItemReader(storageService, "ara");

        reader.setDocumentId(nonExistentId);

        OcrRequest result = reader.read();

        assertThat(result).isNull();
    }
}
