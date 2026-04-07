package com.sofkad.docflow.ocr.domain;

import com.sofkad.docflow.ocr.infrastructure.InMemoryOcrEngine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OcrEngineTest {

    @Test
    void inMemoryOcrEngineShouldReturnText() {
        OcrEngine engine = new InMemoryOcrEngine();
        OcrRequest request = new OcrRequest(new byte[0], 1, "eng");

        OcrResult result = engine.performOcr(request);

        assertThat(result).isNotNull();
        assertThat(result.text()).isNotBlank();
        assertThat(result.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void inMemoryOcrEngineShouldReportAvailable() {
        OcrEngine engine = new InMemoryOcrEngine();

        assertThat(engine.isAvailable()).isTrue();
    }
}
