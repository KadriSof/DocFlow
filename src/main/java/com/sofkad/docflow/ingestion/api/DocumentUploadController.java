package com.sofkad.docflow.ingestion.api;

import com.sofkad.docflow.ingestion.domain.Document;
import com.sofkad.docflow.ingestion.domain.DocumentRepository;
import com.sofkad.docflow.ingestion.infrastructure.FileStorageService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentUploadController {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final JobLauncher jobLauncher;
    private final Job ingestionJob;

    public DocumentUploadController(DocumentRepository documentRepository,
                                    FileStorageService fileStorageService,
                                    JobLauncher jobLauncher,
                                    Job ingestionJob) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.jobLauncher = jobLauncher;
        this.ingestionJob = ingestionJob;
    }

    @PostMapping
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        if (!isPdf(file)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Document document = new Document(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.pdf");
            documentRepository.save(document);

            fileStorageService.storePdf(document.getId(), file.getBytes());

            JobParameters params = new JobParametersBuilder()
                    .addString("documentId", document.getId().toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(ingestionJob, params);

            UploadResponse response = new UploadResponse(
                    document.getId().toString(),
                    "/api/documents/" + document.getId() + "/status"
            );
            return ResponseEntity.accepted().body(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
    public ResponseEntity<DocumentStatusResponse> status(@PathVariable UUID documentId) {
        return documentRepository.findById(documentId)
                .map(doc -> {
                    DocumentStatusResponse response = new DocumentStatusResponse(
                            doc.getId().toString(),
                            doc.getStatus(),
                            doc.getOcrText(),
                            doc.getError()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
