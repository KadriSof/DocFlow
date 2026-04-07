package com.sofkad.docflow.ocr.application;

import com.sofkad.docflow.ocr.domain.OcrEngine;
import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import com.sofkad.docflow.ocr.infrastructure.InMemoryOcrEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OcrServiceTest {

    private OcrService ocrService;
    private OcrEngine ocrEngine;

    @BeforeEach
    void setUp() {
        ocrEngine = new InMemoryOcrEngine();
        ocrService = new OcrService(ocrEngine);
    }

    @Test
    void shouldPerformOcrOnImage() {
        byte[] fakeImage = "%PDF-fake-image-data".getBytes();
        OcrRequest request = new OcrRequest(fakeImage, 1, "eng");

        OcrResult result = ocrService.performOcr(request);

        assertThat(result).isNotNull();
        assertThat(result.text()).isNotBlank();
        assertThat(result.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void shouldProcessMultiplePages() {
        byte[] fakeImage = "fake-image".getBytes();
        OcrResult page1 = ocrService.performOcr(new OcrRequest(fakeImage, 1, "eng"));
        OcrResult page2 = ocrService.performOcr(new OcrRequest(fakeImage, 2, "eng"));

        assertThat(page1.text()).contains("page 1");
        assertThat(page2.text()).contains("page 2");
    }

    @Test
    void shouldReportEngineAvailability() {
        assertThat(ocrService.isAvailable()).isTrue();
    }
}
