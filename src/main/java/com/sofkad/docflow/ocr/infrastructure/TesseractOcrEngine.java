package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrEngine;
import com.sofkad.docflow.ocr.domain.OcrRequest;
import com.sofkad.docflow.ocr.domain.OcrResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Tesseract-based OCR engine adapter.
 * Requires native Tesseract libraries to be installed on the system.
 * Tests for this class should be tagged @Tag("native") and excluded from CI by default.
 */
@Component
@Primary
@Conditional(TesseractAvailabilityCondition.class)
public class TesseractOcrEngine implements OcrEngine {

    private static final Logger log = LoggerFactory.getLogger(TesseractOcrEngine.class);

    private final Tesseract tesseract;

    public TesseractOcrEngine(OcrProperties ocrProperties) {
        this.tesseract = new Tesseract();
        // Set tessdata path (default Tesseract installation on Windows)
        String tessdataPath = System.getenv("TESSDATA_PREFIX");
        if (tessdataPath == null) {
            tessdataPath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
        }
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage(ocrProperties.getDefaultLanguage());
    }

    @Override
    public OcrResult performOcr(OcrRequest request) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(request.image()));
            if (image == null) {
                log.warn("Failed to decode image for page {}", request.pageNumber());
                return new OcrResult("", 0.0);
            }

            String text = tesseract.doOCR(image);
            return new OcrResult(text, 0.85);
        } catch (TesseractException e) {
            log.error("Tesseract OCR failed for page {}: {}", request.pageNumber(), e.getMessage());
            return new OcrResult("", 0.0);
        } catch (IOException e) {
            log.error("Failed to read image for page {}: {}", request.pageNumber(), e.getMessage());
            return new OcrResult("", 0.0);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
