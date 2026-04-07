package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.domain.DocumentType;
import com.sofkad.docflow.processing.domain.ExtractedField;
import com.sofkad.docflow.processing.domain.ExtractionRequest;
import com.sofkad.docflow.processing.domain.ExtractionResult;
import com.sofkad.docflow.processing.domain.ExtractionRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StubExtractionRule implements ExtractionRule {

    @Override
    public boolean supports(DocumentType type) {
        return true;
    }

    @Override
    public ExtractionResult apply(ExtractionRequest request) {
        return new ExtractionResult(
                List.of(new ExtractedField("sample_field", "sample_value", 0.99)),
                true
        );
    }
}
