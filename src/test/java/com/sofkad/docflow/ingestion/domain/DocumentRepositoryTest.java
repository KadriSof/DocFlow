package com.sofkad.docflow.ingestion.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void shouldSaveAndFindDocumentById() {
        Document doc = new Document("test.pdf");
        Document saved = documentRepository.save(doc);

        Optional<Document> found = documentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFilename()).isEqualTo("test.pdf");
        assertThat(found.get().getStatus()).isEqualTo("pending");
    }

    @Test
    void shouldUpdateDocumentStatusToCompleted() {
        Document doc = new Document("arabic_doc.pdf");
        documentRepository.save(doc);

        doc.markCompleted("نص مستخرج من المستند");
        Document updated = documentRepository.save(doc);

        Optional<Document> found = documentRepository.findById(updated.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("completed");
        assertThat(found.get().getOcrText()).contains("نص مستخرج");
    }

    @Test
    void shouldUpdateDocumentStatusToFailed() {
        Document doc = new Document("corrupt.pdf");
        documentRepository.save(doc);

        doc.markFailed("File is corrupted");
        documentRepository.save(doc);

        Optional<Document> found = documentRepository.findById(doc.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("failed");
        assertThat(found.get().getError()).isEqualTo("File is corrupted");
    }
}
