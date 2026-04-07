package com.sofkad.docflow.ocr.domain;

public interface OcrEngine {
    OcrResult performOcr(OcrRequest request);
    boolean isAvailable();
}
