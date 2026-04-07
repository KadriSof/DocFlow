package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based extraction rule for invoice documents.
 * Supports configurable field patterns via ProcessingProperties.
 */
@Component
public class RegexExtractionRule implements ExtractionRule {

    private final Map<String, Pattern> fieldPatterns = Map.of(
            "invoice_number", Pattern.compile("(?:invoice\\s*#?|INV[-#]?)(\\S+)", Pattern.CASE_INSENSITIVE),
            "total_amount", Pattern.compile("(?:total|amount\\s*due)[:\\s]*\\$?([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
            "date", Pattern.compile("(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4})")
    );

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.INVOICE;
    }

    @Override
    public ExtractionResult apply(ExtractionRequest request) {
        List<ExtractedField> fields = new ArrayList<>();

        for (Map.Entry<String, Pattern> entry : fieldPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(request.text());
            if (matcher.find()) {
                fields.add(new ExtractedField(entry.getKey(), matcher.group(1), 0.9));
            }
        }

        return new ExtractionResult(fields, !fields.isEmpty());
    }
}
