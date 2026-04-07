package com.sofkad.docflow.processing.domain;

public interface ExtractionRule {
    boolean supports(DocumentType type);
    ExtractionResult apply(ExtractionRequest request);
}
