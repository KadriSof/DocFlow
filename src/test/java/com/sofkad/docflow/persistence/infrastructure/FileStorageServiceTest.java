package com.sofkad.docflow.persistence.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistPdfFileAndReturnPath() throws IOException {
        FileStorageService storage = new FileStorageService(tempDir);
        byte[] content = "%PDF-1.4 fake pdf".getBytes();

        Path savedPath = storage.store("test-doc-id", content);

        assertThat(savedPath).exists();
        assertThat(savedPath).hasParent(tempDir);
        assertThat(savedPath.getFileName().toString()).isEqualTo("test-doc-id.pdf");
        assertThat(Files.readAllBytes(savedPath)).isEqualTo(content);
    }
}
