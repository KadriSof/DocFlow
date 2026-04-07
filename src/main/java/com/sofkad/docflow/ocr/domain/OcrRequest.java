package com.sofkad.docflow.ocr.domain;

public record OcrRequest(byte[] image, int pageNumber, String language) {
}
