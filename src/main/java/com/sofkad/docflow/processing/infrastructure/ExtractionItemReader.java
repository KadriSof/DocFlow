package com.sofkad.docflow.processing.infrastructure;

import com.sofkad.docflow.processing.domain.ExtractionRequest;
import org.springframework.batch.item.ItemReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExtractionItemReader implements ItemReader<ExtractionRequest> {

    private Iterator<ExtractionRequest> iterator = Collections.emptyIterator();

    public void setInput(List<ExtractionRequest> input) {
        this.iterator = input.iterator();
    }

    @Override
    public ExtractionRequest read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
