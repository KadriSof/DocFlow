package com.sofkad.docflow.ingestion.api;

/**
 * Response DTO for document status endpoint.
 */
public record DocumentStatusResponse(
        String documentId,
        String status,
        String ocrText,
        String error
) {
}
