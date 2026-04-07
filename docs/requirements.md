# DocFlow — Functional & Non-Functional Requirements

## Functional Requirements

### FR-1: Document Upload
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-1.1 | System shall accept PDF files via a REST `POST` endpoint (`multipart/form-data`) | ✅ Implemented | `POST /api/documents` endpoint working |
| FR-1.2 | System shall validate file type (`.pdf` only), size (configurable max), and content integrity | ✅ Partial | Content-Type + magic bytes (`%PDF` prefix) implemented. **TODO:** File size validation missing (NFR-5.2) |
| FR-1.3 | System shall reject invalid uploads with `400 Bad Request` and descriptive error | ✅ Implemented | Validation exceptions return 400 with message |
| FR-1.4 | System shall assign a unique identifier (UUID) to each accepted document | ✅ Implemented | UUID-based document identification |
| FR-1.5 | System shall return `202 Accepted` with the document ID and a job status URL | ✅ Implemented | Returns document ID and status endpoint URL |

**Remaining hardening for FR-1:**
- [ ] Add file size validation (max file size enforcement)
- [ ] Replace in-memory storage with persisted file system storage

---

### FR-2: Asynchronous Processing (Spring Batch)
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-2.1 | Each accepted document shall trigger an asynchronous ingestion job | ✅ Implemented | Job launch wired to upload endpoint |
| FR-2.2 | Job steps shall execute in order: Store → OCR → Extract → Persist | ⚠️ Scaffolded | Dummy step exists. OCR/Extract/Persist steps not yet implemented |
| FR-2.3 | Each step shall process PDFs page-by-page (chunk-oriented, chunk size configurable) | ❌ Not started | Phase 2 (OCR step) |
| FR-2.4 | System shall retry failed operations with configurable retry policy (transient errors) | ❌ Not started | Phase 5 (production hardening) |
| FR-2.5 | System shall skip unreadable/corrupt pages and log them without aborting the full job | ❌ Not started | Phase 5 (production hardening) |
| FR-2.6 | Job status shall be queryable via REST `GET` endpoint by document ID | ✅ Implemented | `GET /api/documents/{id}/status` endpoint working |

---

### FR-3: OCR Processing
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-3.1 | System shall perform OCR on scanned PDF pages to extract text | ❌ Not started | Phase 2 (Tesseract via Tess4J) |
| FR-3.2 | System shall support multi-language text recognition (configurable) | ❌ Not started | Phase 2 (`OcrLanguage` enum planned) |
| FR-3.3 | System shall handle mixed-content pages (text + images) | ❌ Not started | Phase 2 (PDFBox rendering handles all page types) |

---

### FR-4: Data Extraction
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-4.1 | System shall extract structured fields from processed documents | ❌ Not started | Phase 3 (regex-based extraction) |
| FR-4.2 | Extraction rules shall be pluggable/configurable per document type | ❌ Not started | Phase 3 (`ExtractionRule` interface planned) |
| FR-4.3 | System shall produce structured output (JSON) per document | ❌ Not started | Phase 3 (ExtractionResult → JSON) |

---

### FR-5: Persistence & Retrieval
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-5.1 | System shall persist uploaded files with their assigned IDs | ❌ Not started | Phase 4 (persistence module) |
| FR-5.2 | System shall persist extraction results linked to source documents | ❌ Not started | Phase 4 (persistence module) |
| FR-5.3 | System shall provide REST `GET` endpoint to retrieve extraction results by document ID | ❌ Not started | Phase 4 (persistence module) |
| FR-5.4 | System shall provide REST `GET` endpoint to list all processed documents | ❌ Not started | Phase 4 (persistence module) |

---

### FR-6: Error Handling
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-6.1 | System shall return `404 Not Found` for unknown document IDs | ✅ Implemented | Status endpoint returns 404 for unknown IDs |
| FR-6.2 | System shall return `500 Internal Server Error` for unhandled processing failures | ⚠️ Partial | Spring Boot default error handling in place |
| FR-6.3 | Failed documents shall be quarantined with error details accessible via API | ❌ Not started | Phase 5 (error quarantine) |

---

## Non-Functional Requirements

### NFR-1: Reliability
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-1.1 | Jobs are persisted in Spring Batch repository — survives application restarts (resumable) | ⚠️ Partial | Spring Batch configured, but document state is in-memory (`ConcurrentHashMap`). **TODO:** Replace with persisted `Document` entity |
| NFR-1.2 | All external I/O (file read/write, OCR) shall have retry policies with exponential backoff | ❌ Not started | Phase 5 |
| NFR-1.3 | No data loss on transient failure — partial results are saved and marked incomplete | ❌ Not started | Phase 5 (`ExtractionResult.isComplete()` flag planned) |

### NFR-2: Performance
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-2.1 | Upload endpoint response time < 200ms (before job starts) | ✅ Implemented | Lightweight validation + async job launch |
| NFR-2.2 | Chunk size configurable to balance throughput vs. memory (default: 10 pages/chunk) | ❌ Not started | Phase 2 (OCR step config) |
| NFR-2.3 | System shall process documents concurrently up to a configurable thread pool limit | ❌ Not started | Phase 5 (virtual threads planned) |
| NFR-2.4 | Memory footprint shall not grow with document size (streaming/chunk processing) | ❌ Not started | Phase 2 (PDFBox chunk rendering strategy planned) |

### NFR-3: Scalability
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-3.1 | Modular monolith designed to allow future extraction to microservices (Spring Modulith boundaries) | ✅ Implemented | Spring Modulith architecture tests enforce boundaries |
| NFR-3.2 | Batch job infrastructure supports migration to distributed execution (Spring Cloud Data Flow) | ⚠️ Partial | Spring Batch configured, not yet tested with external job repository |

### NFR-4: Observability
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-4.1 | Structured logging (JSON) for all processing stages | ❌ Not started | Phase 5 |
| NFR-4.2 | Metrics exposed via Spring Boot Actuator (job duration, success rate, queue depth) | ❌ Not started | Phase 5 |
| NFR-4.3 | Health endpoint reports batch job status and downstream dependencies | ❌ Not started | Phase 5 |
| NFR-4.4 | Distributed tracing ready (Spring Cloud Sleuth / Micrometer Tracing) | ❌ Not started | Phase 5 |

### NFR-5: Security
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-5.1 | Upload endpoint validates content type header AND file magic bytes (not extension only) | ✅ Implemented | Content-Type check + `%PDF` magic bytes validation |
| NFR-5.2 | File size limits enforced at gateway level and application level | ❌ Not started | **TODO:** Add file size validation in upload endpoint |
| NFR-5.3 | Uploaded files stored outside application deploy path | ❌ Not started | Phase 4 (persistence module) |
| NFR-5.4 | All REST endpoints protected against path traversal and injection | ✅ Implemented | Spring Security defaults + parameterized queries |
| NFR-5.5 | Authentication/authorization integration ready (Spring Security structure, disabled by default for dev) | ❌ Not started | Phase 5 |

### NFR-6: Maintainability
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-6.1 | Code coverage threshold: ≥ 80% (enforced by build) | ❌ Not started | **TODO:** Add JaCoCo plugin (Phase 1) |
| NFR-6.2 | TDD mandatory — no production code without a failing test first | ✅ Enforced | All current code follows Red-Green-Refactor |
| NFR-6.3 | Module boundaries enforced via Spring Modulith architecture tests | ✅ Implemented | `ApplicationModules.verify()` in build |
| NFR-6.4 | API documented via OpenAPI/Swagger annotations | ❌ Not started | **TODO:** Add springdoc-openapi dependency |
| NFR-6.5 | Build passes static analysis (SonarQube / SpotBugs) | ❌ Not started | Phase 5 |

### NFR-7: Testing Strategy
| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-7.1 | **Unit tests** — every class and method (TDD red-green-refactor) | ✅ Enforced | All production code has corresponding tests |
| NFR-7.2 | **Integration tests** — REST endpoints with `@WebMvcTest`, Spring Batch jobs with `@SpringBatchTest` | ✅ Partial | WebMvcTest in place, SpringBatchTest pending real steps |
| NFR-7.3 | **Architecture tests** — Spring Modulith module dependency validation | ✅ Implemented | `ApplicationModules.of().verify()` |
| NFR-7.4 | **Contract tests** — API response schema validation | ❌ Not started | Phase 4 |
| NFR-7.5 | **End-to-end tests** — full upload → process → retrieve flow with embedded database | ❌ Not started | Phase 4 |

---

## New Requirements Identified in Review

The following requirements were identified during the technical architecture review and have been added to the tracking:

| ID | Requirement | Phase | Priority |
|----|-------------|-------|----------|
| NFR-6.1a | JaCoCo plugin enforcement in build pipeline | Phase 1 | 🔴 Critical |
| NFR-2.4a | PDFBox chunk rendering strategy for memory independence | Phase 2 | 🟡 Important |
| NFR-1.3a | `ExtractionResult.isComplete()` flag for partial result tracking | Phase 5 | 🟡 Important |

---

## Critical TODOs

### 🔴 Critical (Phase 1 — Blockers for future phases)
- [ ] **Define `OcrEngine` port interface** — Domain contract for OCR operations
- [ ] **Define `InMemoryOcrEngine` test double** — Enables CI testing without native Tesseract
- [ ] **Define `ExtractionRule` interface** — Pluggable extraction contract
- [ ] **Define stub `ExtractionRule` implementation** — Enables processing module scaffolding
- [ ] **Replace `ConcurrentHashMap` with persisted `Document` entity** — Required for NFR-1.1 (restart resilience)
- [ ] **Add PDF file size validation** — Required for NFR-5.2 (security)
- [ ] **Add JaCoCo plugin** — Code coverage enforcement in build pipeline

### 🟡 Important (Phase 2–3 — Core functionality)
- [ ] **PDFBox-based page-to-image with memory-safe lifecycle** — 150 DPI grayscale, per-chunk rendering
- [ ] **Hybrid inter-module communication** — `JobExecutionContext` for data, `ApplicationEvent` for notifications
- [ ] **Retry policy with exponential backoff** — For transient OCR/extraction failures
- [ ] **Skip policy for corrupt pages** — Log and continue, don't abort full job

### 🟢 Nice-to-Have (Phase 4–5 — Production readiness)
- [ ] Virtual thread pool for concurrent document processing
- [ ] Spring Boot Actuator metrics (job duration, success rate, queue depth)
- [ ] Structured JSON logging for all processing stages
- [ ] Error quarantine API for failed documents
- [ ] springdoc-openapi integration for API documentation

---

## Dependencies

### Current Dependencies
| Dependency | Version | Purpose | Status |
|------------|---------|---------|--------|
| Spring Boot | 3.5.x | Application framework | ✅ Added |
| Spring Modulith | — | Modular architecture | ✅ Added |
| Spring Batch | (via Boot) | Job orchestration | ✅ Added |
| JUnit 5 | (via Boot) | Testing framework | ✅ Added |

### Dependencies to Add
| Dependency | Version | Purpose | Phase | Priority |
|------------|---------|---------|-------|----------|
| **Tess4J** | 5.10+ | Tesseract OCR Java wrapper | Phase 2 | 🔴 |
| **Apache PDFBox** | 3.x | PDF-to-image rendering | Phase 2 | 🔴 |
| **JaCoCo** | (latest) | Code coverage enforcement | Phase 1 | 🔴 |
| **springdoc-openapi** | (latest) | OpenAPI/Swagger documentation | Phase 1 | 🟡 |
| H2 / PostgreSQL | — | Persistent document storage | Phase 4 | 🟡 |

---

## Development Framework

### TDD Compliance
- **Red phase** — write failing test first, verify it fails for the expected reason
- **Green phase** — write minimal code to pass, no over-engineering
- **Refactor phase** — clean up while staying green
- No production code without a corresponding failing test
- Tests use real code paths; mocks only for external systems (DB, file system, OCR service)

### Build Pipeline
```
test → build → architecture-verify → package
```

All stages must pass before commit.

### Architecture Test Enforcement
- `ApplicationModules.of(Application.class).verify()` runs on every build
- Module dependency graph validated — no circular dependencies
- Package visibility enforced — internal implementations hidden
