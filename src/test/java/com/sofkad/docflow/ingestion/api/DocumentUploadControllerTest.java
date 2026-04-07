package com.sofkad.docflow.ingestion.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentUploadController.class)
class DocumentUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAcceptPdfUploadAndReturn202() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4 fake content".getBytes()
        );

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
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4 fake content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isAccepted())
                .andReturn();

        String documentId = uploadResult.getResponse().getContentAsString();
        String id = documentId.split("\"")[3];

        mockMvc.perform(get("/api/documents/" + id + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(id))
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    @Test
    void shouldReturn404ForUnknownDocumentId() throws Exception {
        mockMvc.perform(get("/api/documents/nonexistent/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleConcurrentUploads() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<String>> documentIds = new ArrayList<>();

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
            mockMvc.perform(get("/api/documents/" + documentId + "/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.documentId").value(documentId));
        }
    }
}
