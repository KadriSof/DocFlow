package com.sofkad.docflow.processing.domain;

import com.sofkad.docflow.processing.infrastructure.StubExtractionRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionRuleTest {

    @Test
    void stubExtractionRuleShouldSupportDocumentType() {
        ExtractionRule rule = new StubExtractionRule();

        assertThat(rule.supports(DocumentType.INVOICE)).isTrue();
    }

    @Test
    void stubExtractionRuleShouldExtractFields() {
        ExtractionRule rule = new StubExtractionRule();
        ExtractionRequest request = new ExtractionRequest("sample text", DocumentType.INVOICE);

        ExtractionResult result = rule.apply(request);

        assertThat(result).isNotNull();
        assertThat(result.fields()).isNotEmpty();
        assertThat(result.isComplete()).isTrue();
    }
}
