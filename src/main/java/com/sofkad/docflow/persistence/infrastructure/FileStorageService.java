package com.sofkad.docflow.persistence.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(Path storageRoot) {
        this.storageRoot = storageRoot;
    }

    public Path store(String documentId, byte[] content) throws IOException {
        String fileName = documentId + ".pdf";
        Path target = storageRoot.resolve(fileName);
        Files.write(target, content);
        return target;
    }
}
