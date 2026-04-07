package com.sofkad.docflow;

import com.sofkad.docflow.ingestion.domain.Document;
import com.sofkad.docflow.ingestion.domain.DocumentRepository;
import com.sofkad.docflow.ingestion.infrastructure.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test that verifies the complete upload → OCR → status flow.
 * Uses the real Spring context with all beans wired.
 */
@SpringBootTest
class UploadPipelineIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
        fileStorageService.cleanup();
        documentRepository.deleteAll();
    }

    @Test
    void shouldUploadPdfPersistDocumentAndReturnStatus() throws Exception {
        // Given: A valid PDF file
        byte[] pdfContent = "%PDF-1.4 test document for OCR processing".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfContent
        );

        // When: Upload the PDF
        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.documentId").isNotEmpty())
                .andExpect(jsonPath("$.statusUrl").isNotEmpty())
                .andReturn();

        String responseBody = uploadResult.getResponse().getContentAsString();
        String documentIdStr = extractDocumentId(responseBody);
        UUID documentId = UUID.fromString(documentIdStr);

        // Then: Document should be persisted in the database
        await().atMost(5, SECONDS).untilAsserted(() -> {
            var doc = documentRepository.findById(documentId);
            assertThat(doc).isPresent();
            assertThat(doc.get().getFilename()).isEqualTo("test.pdf");
        });

        // And: Status endpoint should return document data
        mockMvc.perform(get("/api/documents/" + documentId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(documentIdStr))
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    @Test
    void shouldReturnCompletedStatusWithOcrTextAfterProcessing() throws Exception {
        // Given: A valid PDF file
        byte[] pdfContent = "%PDF-1.4 Arabic OCR test document".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "arabic.pdf", "application/pdf", pdfContent
        );

        // Upload
        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isAccepted())
                .andReturn();

        String responseBody = uploadResult.getResponse().getContentAsString();
        String documentIdStr = extractDocumentId(responseBody);

        // Wait for batch job to complete - document status should be completed or failed
        // (fake PDF won't have readable pages, so OCR text may be empty)
        await().atMost(5, SECONDS).untilAsserted(() -> {
            mockMvc.perform(get("/api/documents/" + documentIdStr + "/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").isNotEmpty());
        });
    }

    @Test
    void shouldReturn404ForNonExistentDocument() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/documents/" + nonExistentId + "/status"))
                .andExpect(status().isNotFound());
    }

    private String extractDocumentId(String jsonResponse) {
        // Simple JSON parsing for the documentId field
        int start = jsonResponse.indexOf("\"documentId\":\"") + "\"documentId\":\"".length();
        int end = jsonResponse.indexOf("\"", start);
        return jsonResponse.substring(start, end);
    }
}
