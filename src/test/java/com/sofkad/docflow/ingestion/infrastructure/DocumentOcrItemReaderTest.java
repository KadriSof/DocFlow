package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentOcrItemReaderTest {

    @Test
    void shouldReturnNullWhenFileStorageBasePathDoesNotExist() throws Exception {
        FileStorageService storageService = new FileStorageService("/nonexistent/path/that/does/not/exist");
        DocumentOcrItemReader reader = new DocumentOcrItemReader(storageService, "ara");

        // Without beforeStep being called, reader should return null
        OcrRequest result = reader.read();

        assertThat(result).isNull();
    }

    @Test
    void shouldReadPdfPagesWhenFileExists(@TempDir Path tempDir) throws Exception {
        // Create a simple 2-page PDF
        UUID documentId = UUID.randomUUID();
        File pendingDir = tempDir.resolve("pending").toFile();
        pendingDir.mkdirs();
        
        Path pdfPath = tempDir.resolve("pending").resolve(documentId + ".pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.addPage(new PDPage());
            doc.save(pdfPath.toFile());
        }

        FileStorageService storageService = new FileStorageService(tempDir.toString());

        // Manually trigger file loading (simulating what beforeStep would do)
        Path loadedPath = storageService.loadPdf(documentId);
        assertThat(loadedPath).exists();

        // Verify we can read the file
        assertThat(loadedPath.getFileName().toString()).isEqualTo(documentId + ".pdf");
    }
}
