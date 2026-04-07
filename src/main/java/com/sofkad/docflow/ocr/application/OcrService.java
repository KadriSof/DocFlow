package com.sofkad.docflow.ocr.application;

import com.sofkad.docflow.ocr.domain.OcrEngine;
import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import org.springframework.stereotype.Service;

@Service
public class OcrService {

    private final OcrEngine ocrEngine;

    public OcrService(OcrEngine ocrEngine) {
        this.ocrEngine = ocrEngine;
    }

    public OcrResult performOcr(OcrRequest request) {
        return ocrEngine.performOcr(request);
    }

    public boolean isAvailable() {
        return ocrEngine.isAvailable();
    }
}
