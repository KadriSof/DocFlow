package com.sofkad.docflow.ingestion.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrResult;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ItemWriter that accumulates OCR results and provides access to the combined text.
 * Used by the OcrResultAccumulator to update the Document entity after the step completes.
 */
public class OcrResultAccumulatingWriter implements ItemWriter<OcrResult> {

    private final List<OcrResult> writtenResults = new CopyOnWriteArrayList<>();

    @Override
    public void write(Chunk<? extends OcrResult> chunk) {
        writtenResults.addAll(chunk.getItems());
    }

    public List<OcrResult> getWrittenResults() {
        return List.copyOf(writtenResults);
    }

    public String getCombinedText() {
        StringBuilder sb = new StringBuilder();
        for (OcrResult result : writtenResults) {
            if (result.text() != null && !result.text().isBlank()) {
                if (sb.length() > 0) {
                    sb.append("\n\n--- Page ").append(writtenResults.indexOf(result) + 1).append(" ---\n\n");
                }
                sb.append(result.text());
            }
        }
        return sb.toString();
    }

    public void clear() {
        writtenResults.clear();
    }
}
