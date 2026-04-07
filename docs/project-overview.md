# DocFlow — Project Description, Specs & Modules

## Project Description

DocFlow is a Java/Spring Boot application for document ingestion and data extraction. It accepts PDF file uploads via REST endpoints and processes them to extract structured data. The project follows Test-Driven Development (TDD) and is structured using Spring Modulith for clear module boundaries.

## Specifications

- **Runtime:** Java 21, Spring Boot 3.5.x
- **Architecture:** Spring Modulith (modular monolith)
- **Development Practice:** Test-Driven Development (TDD)
- **Input:** PDF file uploads via REST API
- **Output:** Extracted structured data (JSON)

---

## Architecture

### Spring Modulith Configuration

DocFlow uses **Spring Modulith** to enforce modular monolith architecture:

- **`@ApplicationModule` annotations** — each module declares its role and dependencies explicitly
- **Architecture tests** — `ApplicationModules.of().verify()` enforces module boundaries at build time
- **No circular dependencies** — module dependency graph is strictly directed
- **Package-private visibility** — internal implementation hidden, only public interfaces exposed across modules

### Module Structure

```
docflow/
├── ingestion/        ✅ Implemented   — REST upload, validation, job trigger
├── ocr/              🟡 Planned (Phase 2) — Tesseract OCR via Tess4J
├── processing/       🟡 Planned (Phase 3) — Data extraction & transformation
└── persistence/      🟡 Planned (Phase 4) — Document & result storage
```

### Processing Pipeline (Spring Batch)

DocFlow uses **Spring Batch** to orchestrate the document processing pipeline:

- **Asynchronous job execution** — upload returns immediately, batch job runs in background
- **Chunk-based processing** — PDFs processed page-by-page to manage memory
- **Job lifecycle** — each uploaded document triggers an ingestion job with tracking
- **Retry & skip** — transient failures retried, unreadable pages skipped and logged

---

## Module Details

### `ingestion` Module ✅ Implemented

**Purpose:** Accept PDF uploads, validate, store temporarily, and trigger asynchronous Spring Batch ingestion job.

**REST Endpoints:**
- `POST /api/documents` — Upload PDF (multipart/form-data)
- `GET /api/documents/{id}/status` — Query processing status by document ID

**PDF Validation:**
- Content-Type header check (`application/pdf`)
- Magic bytes validation: file must start with `%PDF` prefix
- Rejects invalid uploads with `400 Bad Request` and descriptive error

**State Management:**
- Thread-safe document tracking via `ConcurrentHashMap.newKeySet()`
- UUID-based document identification
- ⚠️ **TODO:** Replace in-memory tracking with persisted `Document` entity (NFR-1.1)

**Spring Batch Integration:**
- Job scaffolding in place (dummy step exists)
- Asynchronous job launch on successful upload
- Job execution ID returned with `202 Accepted`

**Test Coverage:** 6 tests
- `@WebMvcTest` — REST endpoint validation
- `@SpringBootTest` integration test — full upload flow
- Architecture test — module boundary verification

**Package Structure:**
```
ingestion/
├── api/              — REST controllers (DocumentUploadController)
├── application/      — Application services (DocumentUploadService)
├── domain/           — Domain models (DocumentStatus)
└── infrastructure/   — Batch config, validation utilities
```

---

### `ocr` Module 🟡 Planned (Phase 2)

**Purpose:** Perform Optical Character Recognition on scanned PDF pages using Tesseract OCR engine.

**Architecture:** Port/Adapter pattern
- **Port:** `OcrEngine` interface (domain contract)
- **Adapters:**
  - `TesseractOcrEngine` — production implementation via Tess4J
  - `InMemoryOcrEngine` — test double for CI without native dependencies

**PDF-to-Image Conversion:**
- **Library:** Apache PDFBox 3.x
- **Rendering:** 150 DPI grayscale (balance quality vs. memory)
- **Memory-safe:** Per-chunk lifecycle, images released after OCR step
- **TODO:** Implement PDFBox-based page-to-image with memory-safe lifecycle

**Spring Batch Integration:**
- Chunk-oriented step processing (configurable chunk size)
- Retry policy with exponential backoff for transient OCR failures
- Skip policy for corrupt/unreadable pages (logged, job continues)

**Multi-Language Support:**
- `OcrLanguage` enum for configurable language selection
- Default: English, extensible to Tesseract's language packs

**Package Structure:**
```
ocr/
├── domain/
│   ├── OcrRequest        — Input: PDF page image
│   ├── OcrResult         — Output: recognized text + confidence
│   ├── OcrLanguage       — Enum: supported languages
│   └── OcrEngine         — Port interface
├── application/
│   └── OcrService        — Orchestrates OCR per page/chunk
└── infrastructure/
    ├── TesseractOcrEngine    — Tess4J adapter (@Tag("native"))
    ├── InMemoryOcrEngine     — Test double for unit tests
    ├── OcrStepConfig           — Spring Batch step configuration
    └── OcrProperties             — Configurable properties (chunk size, retry, languages)
```

---

### `processing` Module 🟡 Planned (Phase 3)

**Purpose:** Extract structured data from OCR text output and transform into standardized JSON.

**Extraction Architecture:**
- **Pluggable rules:** `ExtractionRule` interface (regex-based initial implementation)
- **Extensible:** Future implementations (ML-based, template-based) without changing core logic
- **Confidence scoring:** Each extracted field includes confidence metric
- **Partial results:** `ExtractionResult.isComplete()` flag for incomplete extractions

**Document Type Detection:**
- **Hybrid approach:**
  - Explicit type provided on upload (preferred)
  - Heuristic fallback detection when type unknown
- `DocumentTypeDetector` service for heuristic classification

**Spring Batch Integration:**
- Chunk-oriented step following OCR step
- Configurable chunk size (default: 10 pages)
- Outputs structured JSON per document

**Output Format:**
```json
{
  "documentId": "uuid",
  "documentType": "invoice",
  "extractedFields": [
    {
      "fieldName": "totalAmount",
      "value": "1500.00",
      "confidence": 0.95,
      "sourcePage": 1
    }
  ],
  "isComplete": true,
  "warnings": []
}
```

**Package Structure:**
```
processing/
├── domain/
│   ├── ExtractionRequest   — Input: OCR text + document metadata
│   ├── ExtractionResult    — Output: structured fields + confidence
│   ├── ExtractedField      — Individual field with metadata
│   ├── DocumentType        — Enum: supported document types
│   └── ExtractionRule      — Port interface for pluggable rules
├── application/
│   ├── ExtractionService       — Orchestrates extraction pipeline
│   └── DocumentTypeDetector    — Heuristic type detection
└── infrastructure/
    ├── RegexExtractionRule     — Initial rule implementation
    ├── ExtractionStepConfig      — Spring Batch step configuration
    └── ProcessingProperties      — Configurable properties
```

---

## Implementation Plan

| Phase | Focus | Key Deliverables | Status |
|-------|-------|-----------------|--------|
| **Phase 0** | Foundation | Spring Modulith setup, architecture tests, project structure | ✅ Complete |
| **Phase 1** | Port Interfaces & Test Doubles | `OcrEngine` interface, `InMemoryOcrEngine`, `ExtractionRule` interface, JaCoCo setup | 🔴 Ready |
| **Phase 2** | OCR Module (TDD) | Tess4J adapter, PDFBox page-to-image, OCR Spring Batch step, retry/skip policies | ❌ Not started |
| **Phase 3** | Processing Module (TDD) | ExtractionRule implementations, document type detection, extraction Spring Batch step | ❌ Not started |
| **Phase 4** | Integration & Wiring | Module communication, persistence layer, end-to-end flow | ❌ Not started |
| **Phase 5** | Production Hardening | Retry with exponential backoff, partial results, virtual threads, metrics, observability | ❌ Not started |

---

## Architectural Decisions

### Decision 1: Tess4J via Port/Adapter Pattern
- **What:** `OcrEngine` interface in domain, `TesseractOcrEngine` adapter in infrastructure
- **Why:** Enables testing without native Tesseract dependency, allows engine swapping (e.g., cloud OCR services)
- **Test strategy:** `InMemoryOcrEngine` for unit tests, `@Tag("native")` for adapter integration tests

### Decision 2: Apache PDFBox 3.x for PDF-to-Image
- **What:** Use PDFBox 3.x to render PDF pages as images for OCR
- **Why:** Mature, actively maintained, supports PDF 3.x spec, programmatic control over DPI and color space
- **Config:** 150 DPI grayscale (quality vs. memory tradeoff), per-chunk lifecycle to prevent memory growth

### Decision 3: Regex-Based Extraction First, Designed for Pluggability
- **What:** `ExtractionRule` interface with initial `RegexExtractionRule` implementation
- **Why:** Fast to implement, well-understood, covers majority of structured document cases
- **Future:** Interface allows ML-based, template-based, or hybrid rules without refactoring core pipeline

### Decision 4: Hybrid Document Type Detection
- **What:** Explicit type on upload (preferred) with heuristic fallback
- **Why:** Most uploads have known type; fallback handles edge cases and legacy integrations
- **Heuristics:** Keyword matching, layout analysis, field pattern recognition

### Decision 5: Single Job with Multiple Steps
- **What:** One Spring Batch job per document with Store → OCR → Extract → Persist steps
- **Why:** Simpler state management, unified retry/skip policies, easier monitoring
- **Alternative considered:** Separate jobs per phase (rejected: complex state transfer, error handling)

### Decision 6: Hybrid Inter-Module Communication
- **What:** `JobExecutionContext` for passing data between steps, `ApplicationEvent` for cross-module notifications
- **Why:** Batch data stays within job context (efficient), events decouple modules (e.g., "OCR complete" notification)
- **Alternative considered:** Shared database table (rejected: tighter coupling, slower)

### Decision 7: TDD Strategy with Test Doubles
- **What:** `InMemoryOcrEngine` for unit/integration tests, `@Tag("native")` for Tess4J adapter tests
- **Why:** CI pipelines run without native Tesseract dependency, native tests run on-demand or in dedicated stage
- **Coverage:** All business logic tested via doubles, adapter tested separately with `@Tag("native")`

---

## Current Status

**Phase 0 (Foundation):** ✅ Complete
- Spring Modulith properly configured with `@ApplicationModule` annotations
- Architecture tests enforcing module boundaries
- `ingestion` module implemented with REST endpoints, PDF validation, thread-safe state management
- 6 tests passing (WebMvcTest + IntegrationTest + ArchitectureTest)

**Next Steps:** Phase 1 — Define port interfaces and test doubles
- 🔴 Define `OcrEngine` port interface + `InMemoryOcrEngine` test double
- 🔴 Define `ExtractionRule` interface + stub implementation
- 🔴 Add JaCoCo plugin for code coverage enforcement

**Overall Progress:** ~15% complete (1 of 6 phases done)

---

## Ingestion Job Flow

```
Upload → Validate → Store → OCR → Extract → Persist
```

1. **Upload endpoint** receives PDF → validates content-type + magic bytes → stores to `pending/`
2. **Ingestion job** launched asynchronously via Spring Batch
3. **Job steps** (chunk-oriented, page-by-page):
   - `Read` PDF pages → `Process` (OCR + extraction) → `Write` to persistence
4. **Status tracking** — job execution stored in Spring Batch repository, queryable via API
5. **On completion** — document moved to `processed/` or `failed/` with results
