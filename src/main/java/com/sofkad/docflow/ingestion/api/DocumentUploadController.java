package com.sofkad.docflow.ingestion.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/documents")
public class DocumentUploadController {

    private final Set<String> knownDocuments = ConcurrentHashMap.newKeySet();

    @PostMapping
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        if (!isPdf(file)) {
            return ResponseEntity.badRequest().build();
        }

        String documentId = UUID.randomUUID().toString();
        knownDocuments.add(documentId);
        UploadResponse response = new UploadResponse(
                documentId,
                "/api/documents/" + documentId + "/status"
        );
        return ResponseEntity.accepted().body(response);
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return false;
        }

        try {
            byte[] header = file.getBytes();
            return header.length >= 4 &&
                    new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF");
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/{documentId}/status")
    public ResponseEntity<JobStatusResponse> status(@PathVariable String documentId) {
        if (!knownDocuments.contains(documentId)) {
            return ResponseEntity.notFound().build();
        }
        JobStatusResponse response = new JobStatusResponse(documentId, "pending");
        return ResponseEntity.ok(response);
    }
}
