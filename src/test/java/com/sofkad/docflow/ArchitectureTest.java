package com.sofkad.docflow;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTest {

    private final ApplicationModules modules = ApplicationModules.of(DocflowApplication.class);

    @Test
    void modulesShouldBeWellStructured() {
        modules.forEach(System.out::println);
        modules.verify();
    }
}
