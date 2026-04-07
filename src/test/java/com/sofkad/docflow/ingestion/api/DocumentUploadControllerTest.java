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

import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentUploadController.class)
class DocumentUploadControllerTest {

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
    void shouldAcceptPdfUploadAndReturn202() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4 fake content".getBytes()
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorageService.storePdf(any(UUID.class), any(byte[].class)))
                .thenReturn(Path.of("./data/pending/test.pdf"));
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
    void shouldRejectFileWithNonPdfMagicBytes() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                "This is not a PDF file".getBytes()
        );

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnJobStatusForKnownDocumentId() throws Exception {
        Document doc = new Document("test.pdf");
        when(documentRepository.save(any(Document.class))).thenReturn(doc);
        when(fileStorageService.storePdf(any(UUID.class), any(byte[].class)))
                .thenReturn(Path.of("./data/pending/test.pdf"));
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenReturn(new JobExecution(1L));

        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents").file(
                        new MockMultipartFile("file", "test.pdf", "application/pdf", "%PDF-1.4 fake content".getBytes())))
                .andExpect(status().isAccepted())
                .andReturn();

        String response = uploadResult.getResponse().getContentAsString();
        String id = response.split("\"")[3];

        when(documentRepository.findById(doc.getId())).thenReturn(java.util.Optional.of(doc));

        mockMvc.perform(get("/api/documents/" + doc.getId() + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(doc.getId().toString()))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void shouldReturn404ForUnknownDocumentId() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(documentRepository.findById(unknownId)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/documents/" + unknownId + "/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleConcurrentUploads() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<String>> documentIds = new ArrayList<>();

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorageService.storePdf(any(UUID.class), any(byte[].class)))
                .thenReturn(Path.of("./data/pending/test.pdf"));
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenReturn(new JobExecution(1L));

        for (int i = 0; i < threadCount; i++) {
            Future<String> future = executor.submit(() -> {
                try {
                    MockMultipartFile file = new MockMultipartFile(
                            "file",
                            "test" + System.currentTimeMillis() + ".pdf",
                            "application/pdf",
                            "%PDF-1.4 fake content".getBytes()
                    );

                    MvcResult result = mockMvc.perform(multipart("/api/documents").file(file))
                            .andExpect(status().isAccepted())
                            .andReturn();

                    String response = result.getResponse().getContentAsString();
                    return response.split("\"")[3];
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
            documentIds.add(future);
        }

        latch.await();
        executor.shutdown();

        for (Future<String> future : documentIds) {
            String documentId = future.get();
            // Mock the findById for each document ID that was created during concurrent uploads
            Document mockDoc = new Document("test.pdf");
            when(documentRepository.findById(UUID.fromString(documentId))).thenReturn(java.util.Optional.of(mockDoc));
            // Status endpoint may return pending or completed depending on timing
            mockMvc.perform(get("/api/documents/" + documentId + "/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.documentId").isNotEmpty());
        }
    }
}
