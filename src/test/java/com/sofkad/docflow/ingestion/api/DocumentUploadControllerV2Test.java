package com.sofkad.docflow.ingestion.api;

import com.sofkad.docflow.ingestion.domain.Document;
import com.sofkad.docflow.ingestion.domain.DocumentRepository;
import com.sofkad.docflow.ingestion.infrastructure.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentUploadController.class)
class DocumentUploadControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private JobLauncher jobLauncher;

    @MockitoBean
    private Job ingestionJob;

    @Test
    void shouldAcceptPdfUploadSaveAndReturn202() throws Exception {
        UUID docId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4 fake content".getBytes()
        );

        when(fileStorageService.storePdf(any(UUID.class), any(byte[].class)))
                .thenReturn(Path.of("./data/pending/" + docId + ".pdf"));
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenReturn(new JobExecution(1L));

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.documentId").isNotEmpty())
                .andExpect(jsonPath("$.statusUrl").isNotEmpty());
    }

    @Test
    void shouldRejectNonPdfUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "not a pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCompletedStatusWithOcrText() throws Exception {
        Document completedDoc = new Document("test.pdf");
        completedDoc.markCompleted("نص عربي مستخرج");
        UUID docId = completedDoc.getId();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(completedDoc));

        mockMvc.perform(get("/api/documents/" + docId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(docId.toString()))
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.ocrText").value("نص عربي مستخرج"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void shouldReturnFailedStatusWithError() throws Exception {
        Document failedDoc = new Document("bad.pdf");
        failedDoc.markFailed("OCR engine unavailable");
        UUID docId = failedDoc.getId();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(failedDoc));

        mockMvc.perform(get("/api/documents/" + docId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(docId.toString()))
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.error").value("OCR engine unavailable"));
    }

    @Test
    void shouldReturnPendingStatusForInProgressDocument() throws Exception {
        Document pendingDoc = new Document("processing.pdf");
        UUID docId = pendingDoc.getId();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(pendingDoc));

        mockMvc.perform(get("/api/documents/" + docId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(docId.toString()))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.ocrText").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void shouldReturn404ForUnknownDocumentId() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(documentRepository.findById(unknownId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/documents/" + unknownId + "/status"))
                .andExpect(status().isNotFound());
    }
}
