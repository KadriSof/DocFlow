package com.sofkad.docflow.processing.application;

import com.sofkad.docflow.processing.domain.DocumentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentTypeDetector {

    private final List<String> invoiceKeywords;

    public DocumentTypeDetector() {
        this(List.of("invoice", "INV-", "bill", "amount due", "total"));
    }

    public DocumentTypeDetector(List<String> invoiceKeywords) {
        this.invoiceKeywords = invoiceKeywords.isEmpty()
                ? List.of("invoice", "INV-", "bill", "amount due", "total")
                : invoiceKeywords;
    }

    public DocumentType detect(String text) {
        if (text == null || text.isBlank()) {
            return DocumentType.UNKNOWN;
        }

        String lower = text.toLowerCase();

        if (invoiceKeywords.stream().anyMatch(lower::contains)) {
            return DocumentType.INVOICE;
        }

        return DocumentType.UNKNOWN;
    }
}
