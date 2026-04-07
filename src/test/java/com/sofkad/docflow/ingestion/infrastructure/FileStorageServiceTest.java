package com.sofkad.docflow.ingestion.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileStorageService(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        storageService.cleanup();
    }

    @Test
    void shouldStorePdfFileAndReturnPath() throws IOException {
        byte[] pdfContent = "%PDF-1.4 test content".getBytes();
        UUID documentId = UUID.randomUUID();

        Path storedPath = storageService.storePdf(documentId, pdfContent);

        assertThat(storedPath).isNotNull();
        assertThat(Files.exists(storedPath)).isTrue();
        assertThat(Files.readAllBytes(storedPath)).isEqualTo(pdfContent);
    }

    @Test
    void shouldLoadStoredPdfById() throws IOException {
        byte[] pdfContent = "%PDF-1.4 another test".getBytes();
        UUID documentId = UUID.randomUUID();

        storageService.storePdf(documentId, pdfContent);
        Path loadedPath = storageService.loadPdf(documentId);

        assertThat(loadedPath).isNotNull();
        assertThat(Files.exists(loadedPath)).isTrue();
        assertThat(Files.readAllBytes(loadedPath)).isEqualTo(pdfContent);
    }

    @Test
    void shouldThrowWhenLoadingNonExistentPdf() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> storageService.loadPdf(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldStoreFilesInPendingSubdirectory() throws IOException {
        byte[] pdfContent = "%PDF-1.4".getBytes();
        UUID documentId = UUID.randomUUID();

        Path storedPath = storageService.storePdf(documentId, pdfContent);

        assertThat(storedPath.getParent().getFileName().toString()).isEqualTo("pending");
    }

    @Test
    void shouldCleanupRemoveStoredFiles() throws IOException {
        byte[] pdfContent = "%PDF-1.4 cleanup test".getBytes();
        UUID documentId = UUID.randomUUID();

        Path storedPath = storageService.storePdf(documentId, pdfContent);
        assertThat(Files.exists(storedPath)).isTrue();

        storageService.cleanup();

        assertThat(Files.exists(tempDir.resolve("pending"))).isFalse();
    }
}
