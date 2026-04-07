package com.sofkad.docflow.processing.domain;

public record ExtractionRequest(String text, DocumentType documentType) {
    public ExtractionRequest {
        // documentType can be null — will be auto-detected by ExtractionService
    }
}
