package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.domain.DocumentType;
import com.sofkad.docflow.processing.domain.ExtractionRequest;
import com.sofkad.docflow.processing.domain.ExtractionResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexExtractionRuleTest {

    private final RegexExtractionRule rule = new RegexExtractionRule();

    @Test
    void shouldSupportInvoiceType() {
        assertThat(rule.supports(DocumentType.INVOICE)).isTrue();
        assertThat(rule.supports(DocumentType.UNKNOWN)).isFalse();
        assertThat(rule.supports(DocumentType.PASSPORT)).isFalse();
    }

    @Test
    void shouldExtractInvoiceNumberFromText() {
        ExtractionRequest request = new ExtractionRequest("Invoice #INV-2024-001 total amount due", DocumentType.INVOICE);

        ExtractionResult result = rule.apply(request);

        assertThat(result.isComplete()).isTrue();
        assertThat(result.fields()).anyMatch(f -> f.name().equals("invoice_number") && f.value().equals("INV-2024-001"));
    }

    @Test
    void shouldExtractTotalAmountFromText() {
        ExtractionRequest request = new ExtractionRequest("Invoice total: $150.00", DocumentType.INVOICE);

        ExtractionResult result = rule.apply(request);

        assertThat(result.isComplete()).isTrue();
        assertThat(result.fields()).anyMatch(f -> f.name().equals("total_amount") && f.value().equals("150.00"));
    }

    @Test
    void shouldReturnIncompleteForNoMatches() {
        ExtractionRequest request = new ExtractionRequest("this is just plain text nothing special here", DocumentType.INVOICE);

        ExtractionResult result = rule.apply(request);

        assertThat(result.fields()).isEmpty();
        assertThat(result.isComplete()).isFalse();
    }
}
