# DocFlow — Qwen Modus Operandi

## Project Identity

- **Name:** DocFlow
- **Root:** `C:\Vault\Projects\docflow`
- **Stack:** Java 21, Spring Boot 3.5.x, Spring Modulith 1.4.x, Spring Batch
- **Build:** Maven (`mvnw`)
- **Git remote:** `https://github.com/KadriSof/DocFlow.git`
- **Branch:** `main`

## Architecture

### Modules (Spring Modulith)

Each module lives under `com.sofkad.docflow.<module>` with three layers:
- `api` — REST controllers, DTOs, public interfaces
- `domain` — business entities, services, domain logic
- `infrastructure` — repositories, external integrations, persistence

| Module | Status | Responsibility |
|--------|--------|---------------|
| `ingestion` | Started | Upload endpoint, validation, triggers batch job |
| `ocr` | Pending | OCR on PDF pages (batch step) |
| `processing` | Pending | Data extraction pipeline (batch step chain) |
| `persistence` | Pending | Document & result storage (batch ItemWriter) |

### Processing Pipeline

```
Upload → Validate → Store → OCR → Extract → Persist
```

Spring Batch orchestrates this asynchronously. Upload returns `202` with a document ID and status URL. Job runs in background, status queryable via API.

## TDD Workflow — IRON LAW

**No production code without a failing test first.** This is non-negotiable.

### Cycle for every feature

1. **RED** — Write one failing test. Verify it fails for the expected reason (not typos, not compilation errors — missing feature).
2. **GREEN** — Write minimal code to pass. No over-engineering, no YAGNI violations.
3. **REFACTOR** — Clean up while staying green. Improve names, extract helpers, remove duplication.
4. **Verify** — All tests pass, build clean, output pristine.
5. **Log** — Update `docs/tdd-log.md` with the iteration.
6. **Commit** — Atomic commit with descriptive message.

### Red flags — STOP and start over

- Test passes immediately → fix the test
- Test errors instead of failing → fix until it fails correctly
- Code before test → delete, start over
- Adding features beyond the test → stop, remove

### Testing conventions

- Use `@WebMvcTest` for controller tests (sliced context, no full boot)
- Use `@SpringBatchTest` for batch job tests
- Use `@SpringBootTest` only for full integration / E2E tests
- Real code paths preferred; mocks only for external systems (DB, FS, OCR service)
- Tests live in mirrored package: `src/test/java/com/sofkad/docflow/<module>/...`

## Workflow Rules

### On session start
1. Read `docs/project-overview.md` for architecture context
2. Read `docs/requirements.md` for functional/non-functional requirements
3. Read `docs/tdd-log.md` for last iteration status
4. Check `git status` and `git log -n 3` for current state
5. Run full test suite (`mvn test`) to establish baseline

### Before commit
1. `mvn test` — all green
2. `git status` — review staged files
3. `git diff --staged --stat` — verify scope
4. Atomic commit, then `git status` to confirm clean

### Documentation
- `docs/project-overview.md` — architecture, modules, pipeline design
- `docs/requirements.md` — FR + NFR specs
- `docs/tdd-log.md` — per-iteration test tracking (RED/GREEN results)
- Update docs before code commits

### Dependencies
- Added `spring-boot-starter-web` (was missing from initial Spring Initializr setup)
- `spring-boot-starter-batch` already present
- `spring-modulith-starter-core` + `spring-modulith-starter-test` present
- Lombok with annotation processor configured

## Current State

### Completed
- [x] Initial commit + push to GitHub
- [x] Project docs: `project-overview.md`, `requirements.md`
- [x] Iteration 1: Upload endpoint (`POST /api/documents`)
  - Accepts PDF → `202` with `documentId` + `statusUrl`
  - Rejects non-PDF → `400`
  - Content-type validation only (no magic bytes yet)

### Next on deck
- [ ] Job status endpoint (`GET /api/documents/{id}/status`)
- [ ] Persist uploaded files to disk (infrastructure layer)
- [ ] Spring Batch ingestion job scaffolding
- [ ] Magic bytes validation (NFR-5.1)

### Build status
- `mvn test` passes (2 tests)
- No compilation warnings
- JDK 21, Maven via `mvnw`

## Notes & Decisions

- Spring Modulith module verification via `ApplicationModules.verify()` kept out of `main()` — belongs in architecture tests instead (add later)
- `@WebMvcTest` used for controller tests — loads only web layer, fast feedback
- File storage path not yet configured — will use `application.properties` when infrastructure layer is built
- Git line ending warnings (LF→CRLF) are cosmetic on Windows, `.gitattributes` handles `mvnw` correctly
