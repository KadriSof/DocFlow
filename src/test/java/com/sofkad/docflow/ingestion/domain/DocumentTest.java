package com.sofkad.docflow.ingestion.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    @Test
    void shouldCreateDocumentWithPendingStatus() {
        Document doc = new Document("test.pdf");

        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getFilename()).isEqualTo("test.pdf");
        assertThat(doc.getStatus()).isEqualTo("pending");
        assertThat(doc.getOcrText()).isNull();
        assertThat(doc.getError()).isNull();
        assertThat(doc.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldMarkDocumentAsCompletedWithOcrText() {
        Document doc = new Document("test.pdf");

        doc.markCompleted("extracted Arabic text هنا");

        assertThat(doc.getStatus()).isEqualTo("completed");
        assertThat(doc.getOcrText()).isEqualTo("extracted Arabic text هنا");
        assertThat(doc.getError()).isNull();
    }

    @Test
    void shouldMarkDocumentAsFailedWithError() {
        Document doc = new Document("test.pdf");

        doc.markFailed("OCR engine not available");

        assertThat(doc.getStatus()).isEqualTo("failed");
        assertThat(doc.getError()).isEqualTo("OCR engine not available");
    }
}
