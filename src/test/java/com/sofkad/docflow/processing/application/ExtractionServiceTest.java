package com.sofkad.docflow.processing.application;

import com.sofkad.docflow.processing.domain.*;
import com.sofkad.docflow.processing.infrastructure.StubExtractionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionServiceTest {

    private ExtractionService extractionService;

    @BeforeEach
    void setUp() {
        ExtractionRule stubRule = new StubExtractionRule();
        DocumentTypeDetector detector = new DocumentTypeDetector(List.of());
        extractionService = new ExtractionService(List.of(stubRule), detector);
    }

    @Test
    void shouldExtractFieldsFromText() {
        ExtractionRequest request = new ExtractionRequest("invoice total: $150.00", DocumentType.INVOICE);

        ExtractionResult result = extractionService.extract(request);

        assertThat(result).isNotNull();
        assertThat(result.fields()).isNotEmpty();
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    void shouldDetectDocumentTypeWhenNotProvided() {
        DocumentTypeDetector detector = new DocumentTypeDetector(List.of("invoice", "INV-", "bill"));
        ExtractionRule stubRule = new StubExtractionRule();
        ExtractionService service = new ExtractionService(List.of(stubRule), detector);

        ExtractionRequest request = new ExtractionRequest("INVOICE #12345 total amount due", null);
        ExtractionResult result = service.extract(request);

        assertThat(result).isNotNull();
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    void shouldReturnUnknownTypeForUnrecognizedContent() {
        DocumentTypeDetector detector = new DocumentTypeDetector(List.of("invoice", "passport"));
        ExtractionRule stubRule = new StubExtractionRule();
        ExtractionService service = new ExtractionService(List.of(stubRule), detector);

        DocumentType detected = detector.detect("random unknown content here");

        assertThat(detected).isEqualTo(DocumentType.UNKNOWN);
    }
}
