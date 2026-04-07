package com.sofkad.docflow.processing.domain;

import java.util.List;

public record ExtractionResult(List<ExtractedField> fields, boolean isComplete) {
}
