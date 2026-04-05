# DocFlow — Functional & Non-Functional Requirements

## Functional Requirements

### FR-1: Document Upload
- **FR-1.1** System shall accept PDF files via a REST `POST` endpoint (`multipart/form-data`)
- **FR-1.2** System shall validate file type (`.pdf` only), size (configurable max), and content integrity
- **FR-1.3** System shall reject invalid uploads with `400 Bad Request` and descriptive error
- **FR-1.4** System shall assign a unique identifier (UUID) to each accepted document
- **FR-1.5** System shall return `202 Accepted` with the document ID and a job status URL

### FR-2: Asynchronous Processing (Spring Batch)
- **FR-2.1** Each accepted document shall trigger an asynchronous ingestion job
- **FR-2.2** Job steps shall execute in order: Store → OCR → Extract → Persist
- **FR-2.3** Each step shall process PDFs page-by-page (chunk-oriented, chunk size configurable)
- **FR-2.4** System shall retry failed operations with configurable retry policy (transient errors)
- **FR-2.5** System shall skip unreadable/corrupt pages and log them without aborting the full job
- **FR-2.6** Job status shall be queryable via REST `GET` endpoint by document ID

### FR-3: OCR Processing
- **FR-3.1** System shall perform OCR on scanned PDF pages to extract text
- **FR-3.2** System shall support multi-language text recognition (configurable)
- **FR-3.3** System shall handle mixed-content pages (text + images)

### FR-4: Data Extraction
- **FR-4.1** System shall extract structured fields from processed documents
- **FR-4.2** Extraction rules shall be pluggable/configurable per document type
- **FR-4.3** System shall produce structured output (JSON) per document

### FR-5: Persistence & Retrieval
- **FR-5.1** System shall persist uploaded files with their assigned IDs
- **FR-5.2** System shall persist extraction results linked to source documents
- **FR-5.3** System shall provide REST `GET` endpoint to retrieve extraction results by document ID
- **FR-5.4** System shall provide REST `GET` endpoint to list all processed documents

### FR-6: Error Handling
- **FR-6.1** System shall return `404 Not Found` for unknown document IDs
- **FR-6.2** System shall return `500 Internal Server Error` for unhandled processing failures
- **FR-6.3** Failed documents shall be quarantined with error details accessible via API

---

## Non-Functional Requirements

### NFR-1: Reliability
- **NFR-1.1** Jobs are persisted in Spring Batch repository — survives application restarts (resumable)
- **NFR-1.2** All external I/O (file read/write, OCR) shall have retry policies with exponential backoff
- **NFR-1.3** No data loss on transient failure — partial results are saved and marked incomplete

### NFR-2: Performance
- **NFR-2.1** Upload endpoint response time < 200ms (before job starts)
- **NFR-2.2** Chunk size configurable to balance throughput vs. memory (default: 10 pages/chunk)
- **NFR-2.3** System shall process documents concurrently up to a configurable thread pool limit
- **NFR-2.4** Memory footprint shall not grow with document size (streaming/chunk processing)

### NFR-3: Scalability
- **NFR-3.1** Modular monolith designed to allow future extraction to microservices (Spring Modulith boundaries)
- **NFR-3.2** Batch job infrastructure supports migration to distributed execution (Spring Cloud Data Flow)

### NFR-4: Observability
- **NFR-4.1** Structured logging (JSON) for all processing stages
- **NFR-4.2** Metrics exposed via Spring Boot Actuator (job duration, success rate, queue depth)
- **NFR-4.3** Health endpoint reports batch job status and downstream dependencies
- **NFR-4.4** Distributed tracing ready (Spring Cloud Sleave / Micrometer Tracing)

### NFR-5: Security
- **NFR-5.1** Upload endpoint validates content type header AND file magic bytes (not extension only)
- **NFR-5.2** File size limits enforced at gateway level and application level
- **NFR-5.3** Uploaded files stored outside application deploy path
- **NFR-5.4** All REST endpoints protected against path traversal and injection
- **NFR-5.5** Authentication/authorization integration ready (Spring Security structure, disabled by default for dev)

### NFR-6: Maintainability
- **NFR-6.1** Code coverage threshold: ≥ 80% (enforced by build)
- **NFR-6.2** TDD mandatory — no production code without a failing test first
- **NFR-6.3** Module boundaries enforced via Spring Modulith architecture tests
- **NFR-6.4** API documented via OpenAPI/Swagger annotations
- **NFR-6.5** Build passes static analysis (SonarQube / SpotBugs)

### NFR-7: Testing Strategy
- **NFR-7.1** **Unit tests** — every class and method (TDD red-green-refactor)
- **NFR-7.2** **Integration tests** — REST endpoints with `@WebMvcTest`, Spring Batch jobs with `@SpringBatchTest`
- **NFR-7.3** **Architecture tests** — Spring Modulith module dependency validation
- **NFR-7.4** **Contract tests** — API response schema validation
- **NFR-7.5** **End-to-end tests** — full upload → process → retrieve flow with embedded database

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
