# DocFlow — Project Description, Specs & Modules

## Project Description

DocFlow is a Java/Spring Boot application for document ingestion and data extraction. It accepts PDF file uploads via REST endpoints and processes them to extract structured data. The project follows Test-Driven Development (TDD) and is structured using Spring Modulith for clear module boundaries.

## Specifications

- **Runtime:** Java 21, Spring Boot 3.5.x
- **Architecture:** Spring Modulith (modular monolith)
- **Development Practice:** Test-Driven Development (TDD)
- **Input:** PDF file uploads via REST API
- **Output:** Extracted structured data

## Modules

### Processing Pipeline (Spring Batch)

DocFlow uses **Spring Batch** to orchestrate the document processing pipeline:

- **Asynchronous job execution** — upload returns immediately, batch job runs in background
- **Chunk-based processing** — PDFs processed page-by-page to manage memory
- **Job lifecycle** — each uploaded document triggers an ingestion job with tracking
- **Retry & skip** — transient failures retried, unreadable pages skipped and logged

### Planned Modules

| Module | Responsibility |
|--------|---------------|
| `ingestion` | REST endpoints for PDF upload, validation, triggers Spring Batch ingestion job |
| `ocr` | Optical Character Recognition on uploaded PDFs (batch step) |
| `processing` | Data extraction and transformation pipeline (batch step chain) |
| `persistence` | Document and extraction result management (DB/FS) (batch ItemWriter) |

### Ingestion Job Flow

```
Upload → Validate → Store → OCR → Extract → Persist
```

1. **Upload endpoint** receives PDF → validates → stores to `pending/`
2. **Ingestion job** launched asynchronously via Spring Batch
3. **Job steps** (chunk-oriented, page-by-page):
   - `Read` PDF pages → `Process` (OCR + extraction) → `Write` to persistence
4. **Status tracking** — job execution stored in Spring Batch repository, queryable via API
5. **On completion** — document moved to `processed/` or `failed/` with results
