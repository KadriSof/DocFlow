package com.sofkad.docflow.processing.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "docflow.processing")
public class ProcessingProperties {

    private int chunkSize = 10;
    private int skipLimit = 3;
    private List<String> invoiceKeywords = new java.util.ArrayList<>(List.of("invoice", "INV-", "bill", "amount due", "total"));

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getSkipLimit() {
        return skipLimit;
    }

    public void setSkipLimit(int skipLimit) {
        this.skipLimit = skipLimit;
    }

    public List<String> getInvoiceKeywords() {
        return invoiceKeywords;
    }

    public void setInvoiceKeywords(List<String> invoiceKeywords) {
        this.invoiceKeywords = invoiceKeywords;
    }
}
