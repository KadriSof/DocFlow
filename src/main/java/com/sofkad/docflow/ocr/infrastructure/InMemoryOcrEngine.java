package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrEngine;
import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.springframework.stereotype.Component;

@Component
public class InMemoryOcrEngine implements OcrEngine {

    @Override
    public OcrResult performOcr(OcrRequest request) {
        return new OcrResult("sample extracted text from page " + request.pageNumber(), 0.95);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
