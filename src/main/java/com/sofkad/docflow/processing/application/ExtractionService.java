package com.sofkad.docflow.processing.application;

import com.sofkad.docflow.processing.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExtractionService {

    private final List<ExtractionRule> rules;
    private final DocumentTypeDetector typeDetector;

    public ExtractionService(List<ExtractionRule> rules, DocumentTypeDetector typeDetector) {
        this.rules = rules;
        this.typeDetector = typeDetector;
    }

    public ExtractionResult extract(ExtractionRequest request) {
        DocumentType type = request.documentType() != null
                ? request.documentType()
                : typeDetector.detect(request.text());

        ExtractionRequest resolvedRequest = new ExtractionRequest(request.text(), type);

        return rules.stream()
                .filter(rule -> rule.supports(type))
                .findFirst()
                .map(rule -> rule.apply(resolvedRequest))
                .orElse(new ExtractionResult(List.of(), false));
    }
}
