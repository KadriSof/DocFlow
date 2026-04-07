package com.sofkad.docflow.ingestion.infrastructure;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for storing and loading uploaded PDF files on the filesystem.
 */
@Service
public class FileStorageService {

    private final Path pendingDir;

    public FileStorageService() {
        this("./data");
    }

    public FileStorageService(String basePath) {
        this.pendingDir = Paths.get(basePath, "pending");
    }

    /**
     * Stores a PDF file for the given document ID.
     *
     * @param documentId unique identifier for the document
     * @param pdfContent the PDF file bytes
     * @return the path where the file was stored
     */
    public Path storePdf(UUID documentId, byte[] pdfContent) throws IOException {
        Files.createDirectories(pendingDir);
        Path filePath = pendingDir.resolve(documentId + ".pdf");
        Files.write(filePath, pdfContent);
        return filePath;
    }

    /**
     * Loads a stored PDF file by document ID.
     *
     * @param documentId unique identifier for the document
     * @return the path to the stored PDF file
     * @throws IllegalArgumentException if the file does not exist
     */
    public Path loadPdf(UUID documentId) {
        Path filePath = pendingDir.resolve(documentId + ".pdf");
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("PDF file not found for document: " + documentId);
        }
        return filePath;
    }

    /**
     * Removes all stored files in the pending directory.
     */
    public void cleanup() {
        if (Files.exists(pendingDir)) {
            try {
                Files.walk(pendingDir)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                // ignore during cleanup
                            }
                        });
            } catch (IOException e) {
                // ignore during cleanup
            }
        }
    }
}
