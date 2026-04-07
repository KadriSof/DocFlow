package com.sofkad.docflow.ocr.infrastructure;

import com.sofkad.docflow.ocr.domain.OcrRequest;
import org.springframework.batch.item.ItemReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OcrItemReader implements ItemReader<OcrRequest> {

    private Iterator<OcrRequest> iterator = Collections.emptyIterator();

    public void setInput(List<OcrRequest> input) {
        this.iterator = input.iterator();
    }

    @Override
    public OcrRequest read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
