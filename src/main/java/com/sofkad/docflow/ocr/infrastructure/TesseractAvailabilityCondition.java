package com.sofkad.docflow.ocr.infrastructure;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that checks if native Tesseract libraries are available on the system.
 */
public class TesseractAvailabilityCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            net.sourceforge.tess4j.Tesseract tesseract = new net.sourceforge.tess4j.Tesseract();
            tesseract.setDatapath(System.getenv("TESSDATA_PREFIX"));
            return true;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            return false;
        }
    }
}
