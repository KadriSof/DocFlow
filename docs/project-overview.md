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

### Planned Modules

| Module | Responsibility |
|--------|---------------|
| `upload` | REST endpoints for PDF file upload, validation, and storage |
| `extraction` | Data extraction logic from uploaded PDFs |
| `storage` | Persisted document and extraction result management |
| `api` | Public REST API definitions and DTOs |
