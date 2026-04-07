package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.batch.item.ItemReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Spring Batch ItemReader that reads PDF pages one at a time,
 * rendering each page as a grayscale image at 150 DPI for OCR processing.
 * Memory-safe: opens PDDocument once per setPdfFile call, flushes images after reading.
 */
public class PdfPageItemReader implements ItemReader<OcrRequest> {

    private static final int DPI = 150;

    private final String defaultLanguage;
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPageIndex = 0;

    public PdfPageItemReader(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public void setPdfFile(File pdfFile) throws Exception {
        close();
        this.document = Loader.loadPDF(pdfFile);
        this.renderer = new PDFRenderer(document);
        this.currentPageIndex = 0;
    }

    @Override
    public OcrRequest read() throws Exception {
        if (document == null || currentPageIndex >= document.getNumberOfPages()) {
            return null;
        }

        int pageNumber = currentPageIndex + 1;

        BufferedImage image = renderer.renderImageWithDPI(currentPageIndex, DPI, ImageType.GRAY);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            currentPageIndex++;
            return new OcrRequest(imageBytes, pageNumber, defaultLanguage);
        } finally {
            image.flush();
        }
    }

    public void close() throws Exception {
        if (document != null) {
            document.close();
            document = null;
            renderer = null;
            currentPageIndex = 0;
        }
    }

    public int getNumberOfPages() {
        return document != null ? document.getNumberOfPages() : 0;
    }
}
