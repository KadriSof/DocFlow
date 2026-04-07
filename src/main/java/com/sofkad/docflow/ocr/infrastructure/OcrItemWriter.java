package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrResult;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class OcrItemWriter implements ItemWriter<OcrResult> {

    private final List<OcrResult> writtenResults = new ArrayList<>();

    @Override
    public void write(Chunk<? extends OcrResult> chunk) {
        writtenResults.addAll(chunk.getItems());
    }

    public List<OcrResult> getWrittenResults() {
        return List.copyOf(writtenResults);
    }

    public void clear() {
        writtenResults.clear();
    }
}
