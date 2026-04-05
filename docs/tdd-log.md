# DocFlow — TDD Test Tracking Log

## Iteration 1: Upload Endpoint (ingestion module)

### Feature: Accept PDF uploads via POST /api/documents

| # | Requirement | Phase | Test | Result | Notes |
|---|-------------|-------|------|--------|-------|
| 1 | FR-1.1 — Accept PDF via multipart POST | RED | `shouldAcceptPdfUploadAndReturn202` | ✅ Fails (class not found) | `DocumentUploadController` did not exist |
| 1 | FR-1.1 — Accept PDF via multipart POST | GREEN | `shouldAcceptPdfUploadAndReturn202` | ✅ Passes | Minimal controller + `UploadResponse` record |
| 2 | FR-1.3 — Reject non-PDF uploads | RED | `shouldRejectNonPdfUpload` | ✅ Fails (202 ≠ 400) | Controller accepted everything |
| 2 | FR-1.3 — Reject non-PDF uploads | GREEN | `shouldRejectNonPdfUpload` | ✅ Passes | Added `isPdf()` content-type check |

**Commit:** `488ab0d` — `feat(ingestion): add PDF upload endpoint with validation (TDD)`

**Files:**
- `src/main/java/com/sofkad/docflow/ingestion/api/DocumentUploadController.java`
- `src/main/java/com/sofkad/docflow/ingestion/api/UploadResponse.java`
- `src/test/java/com/sofkad/docflow/ingestion/api/DocumentUploadControllerTest.java`

---

## Iteration 2: Job Status Endpoint (ingestion module)

### Feature: Query job status via GET /api/documents/{id}/status

| # | Requirement | Phase | Test | Result | Notes |
|---|-------------|-------|------|--------|-------|
| 3 | FR-2.6 — Return status for known document | RED | `shouldReturnJobStatusForKnownDocumentId` | ✅ Fails (200 ≠ 404) | No status endpoint existed |
| 3 | FR-2.6 — Return status for known document | GREEN | `shouldReturnJobStatusForKnownDocumentId` | ✅ Passes | Added `GET /{id}/status`, in-memory `Set` tracker |
| 4 | FR-6.1 — Return 404 for unknown document | RED | `shouldReturn404ForUnknownDocumentId` | ✅ Fails (404 ≠ 200) | Endpoint returned "pending" for everything |
| 4 | FR-6.1 — Return 404 for unknown document | GREEN | `shouldReturn404ForUnknownDocumentId` | ✅ Passes | Added `knownDocuments.contains()` check |

**Commit:** _pending_

**Files:**
- `src/main/java/com/sofkad/docflow/ingestion/api/DocumentUploadController.java` — added status endpoint + in-memory store
- `src/main/java/com/sofkad/docflow/ingestion/api/JobStatusResponse.java` — new DTO
- `src/test/java/com/sofkad/docflow/ingestion/api/DocumentUploadControllerTest.java` — 2 new tests

---

## Iteration 3: File Storage Service (persistence module)

### Feature: Persist uploaded files to disk (FR-5.1)

| # | Requirement | Phase | Test | Result | Notes |
|---|-------------|-------|------|--------|-------|
| 5 | FR-5.1 — Persist uploaded file to disk | RED | `shouldPersistPdfFileAndReturnPath` | ✅ Fails (class not found) | `FileStorageService` did not exist |
| 5 | FR-5.1 — Persist uploaded file to disk | GREEN | `shouldPersistPdfFileAndReturnPath` | ✅ Passes | `FileStorageService` in `persistence/infrastructure` |

**Commit:** _pending_

**Files:**
- `src/main/java/com/sofkad/docflow/persistence/infrastructure/FileStorageService.java` — new
- `src/test/java/com/sofkad/docflow/persistence/infrastructure/FileStorageServiceTest.java` — new
- `pom.xml` — added H2 test dependency for Spring Boot context in `@SpringBootTest`

**Note:** Initially placed in `ingestion/infrastructure` — corrected to `persistence` module per Spring Modulith module boundaries.

---

## Iteration 4: _Pending_

<!-- Next iteration will go here -->
