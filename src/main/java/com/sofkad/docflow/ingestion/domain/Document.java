package com.sofkad.docflow.ingestion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity representing an uploaded document being processed through the OCR pipeline.
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ocrText;

    private String error;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Document() {
        // JPA required
    }

    public Document(String filename) {
        this.id = UUID.randomUUID();
        this.filename = filename;
        this.status = "pending";
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getStatus() {
        return status;
    }

    public String getOcrText() {
        return ocrText;
    }

    public String getError() {
        return error;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markCompleted(String ocrText) {
        this.status = "completed";
        this.ocrText = ocrText;
    }

    public void markFailed(String error) {
        this.status = "failed";
        this.error = error;
    }
}
