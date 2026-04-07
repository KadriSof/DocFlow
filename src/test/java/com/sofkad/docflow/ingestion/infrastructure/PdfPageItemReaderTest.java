package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PdfPageItemReaderTest {

    private PdfPageItemReader reader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reader = new PdfPageItemReader("eng");
    }

    @Test
    void shouldReadPagesFromPdf() throws Exception {
        File pdfFile = createSimplePdf(3);
        reader.setPdfFile(pdfFile);

        OcrRequest page1 = reader.read();
        OcrRequest page2 = reader.read();
        OcrRequest page3 = reader.read();
        OcrRequest endOfStream = reader.read();

        assertThat(page1).isNotNull();
        assertThat(page1.pageNumber()).isEqualTo(1);
        assertThat(page1.language()).isEqualTo("eng");
        assertThat(page1.image()).isNotEmpty();

        assertThat(page2.pageNumber()).isEqualTo(2);
        assertThat(page3.pageNumber()).isEqualTo(3);
        assertThat(endOfStream).isNull();
    }

    @Test
    void shouldReturnNullForEmptyDocument() throws Exception {
        File pdfFile = createSimplePdf(0);
        reader.setPdfFile(pdfFile);

        assertThat(reader.read()).isNull();
    }

    @Test
    void shouldResetReaderForNewDocument() throws Exception {
        File pdfFile1 = createSimplePdf(2);
        File pdfFile2 = createSimplePdf(1);

        reader.setPdfFile(pdfFile1);
        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();

        reader.setPdfFile(pdfFile2);
        OcrRequest page = reader.read();
        assertThat(page).isNotNull();
        assertThat(page.pageNumber()).isEqualTo(1);
    }

    private File createSimplePdf(int pageCount) throws IOException {
        File pdfFile = tempDir.resolve("test_" + System.currentTimeMillis() + ".pdf").toFile();
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pageCount; i++) {
                document.addPage(new PDPage());
            }
            document.save(pdfFile);
        }
        return pdfFile;
    }
}
