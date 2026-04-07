package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.domain.ExtractionResult;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class ExtractionItemWriter implements ItemWriter<ExtractionResult> {

    private final List<ExtractionResult> writtenResults = new ArrayList<>();

    @Override
    public void write(Chunk<? extends ExtractionResult> chunk) {
        writtenResults.addAll(chunk.getItems());
    }

    public List<ExtractionResult> getWrittenResults() {
        return List.copyOf(writtenResults);
    }

    public void clear() {
        writtenResults.clear();
    }
}
