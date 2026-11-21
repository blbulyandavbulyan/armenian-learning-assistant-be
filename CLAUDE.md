# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Backend for an Armenian language learning assistant: Spring Boot 4, Java 25, Spring AI (Google Gemini for dialogue generation and embeddings), PostgreSQL + pgvector, Flyway, Piper TTS (separate HTTP service) for Armenian speech synthesis. Base package: `com.blbulyandavbulyan.larm`.

## Commands

```bash
docker compose up -d                 # Postgres (pgvector) on :5432 + Piper TTS on :5000
mvn clean compile                    # also runs Checkstyle + PMD (both bound to compile, build fails on violations) and Hibernate bytecode enhancement
mvn verify                           # unit tests (surefire) + integration tests (*IT, failsafe) — use this, not `mvn test`
mvn test -Dtest=ArchitectureTest     # single unit test
mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=DialogueControllerIT   # single IT
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # mutation testing (80% threshold, history in .pitest/)
```

- Integration tests need Docker (Testcontainers starts `pgvector/pgvector:pg17`).
- Running the app locally: Spring profile `local`, env vars `GEMINI_API_KEY` and `STORAGE_FOLDER_NAME` (existing directory for stored audio files; default is a Linux path). JWT auth is validated against a Supabase issuer configured in `application-local.yaml`.
- Hibernate bytecode enhancement (`hibernate-maven-plugin`, `compile` phase) is required for lazy-loading large fields such as embeddings. In IntelliJ, don't delegate build to Maven; instead add a "Run Maven Goal: compile" step *after* "Build" in the run config's "Before launch" list, otherwise IDE builds overwrite the enhanced classes.
- CI (`.github/workflows/maven.yml`): compile → test → verify → Pitest → SonarCloud.

## Architecture

Request flow: `api` (controllers) → `core` (orchestrators) / domain services → `dao` (JPA entities + repositories).

- **`api`**: outward-facing layer. Each feature subpackage has a `*Api` interface carrying all Spring MVC mapping + Swagger/OpenAPI annotations, a package-private `*Controller` implementing it, request/response records, and hand-written `@Component` mappers converting to/from internal resources. Error handling lives in `api.advice`.
- **`core`**: cross-domain orchestration. `DialogueOrchestrator.saveDialogue` vectorizes the dialogue (embedding), runs each title/speaker/phrase through `PhraseProcessor` (TTS via Piper → store audio via `ObjectStorageService` → build media metadata), then persists through `DialogueSavingService`.
- **`ai`**: `ai.chat.DialogueChatService` calls Gemini via `ChatClient` with the system prompt `src/main/resources/prompts/ARMENIAN-DIALOGUE-GENERATOR.md`, chat memory (window of 10, keyed by `userId:chatId`), and `JakartaValidationAdvisor`, which validates the structured output with Jakarta Validation and re-prompts the LLM (up to 5 attempts) before throwing `UnfixableValidationException`. `ai.embedding` builds dialogue embeddings; `ai.tts` is the Piper REST client.
- **`dialogue`, `phrase`, `asset`, `user`, `storage`**: domain services. Phrases are deduplicated by text on save (`PhraseStoringService`). `storage.local.LocalObjectStorageService` is the only storage impl and guards against path traversal.
- **`security`**: OAuth2 resource server (JWT); `DatabaseUserJwtConverter` maps the JWT to a DB user, exposed to controllers as `DatabaseUserJwtAuthenticationToken`. `app.security.enabled=false` permits all requests.
- **Schema** is managed only by Flyway (`src/main/resources/db/migration`); add a new `V<n>__*.sql` rather than editing existing ones.

### Enforced architectural rules (`ArchitectureTest`)

- Nothing outside `api` may depend on `api` (checked both in bytecode via ArchUnit and in source via JavaParser, to catch inlined constants). Internal code uses its own `*Resource`/`*Parameters` records instead of API DTOs.
- Swagger annotations (`io.swagger..`) only inside `api`.
- `api.chat`, `api.assets`, `api.phrases` must not depend on each other; `api.advice` and `api.openapi` are self-contained.

### Testing conventions

- Integration tests extend `BaseIT`: Testcontainers Postgres, MockMvc, WireMock for Piper (`PiperWireMock`), and `@MockitoBean` for the Gemini `Client`, `EmbeddingModel` and `JwtDecoder`. After each test it truncates data (`sql-test-scripts/drop-all-data-after-test.sql`), clears caches and the temp storage dir.
- Test data is seeded with SQL scripts in `src/test/resources/sql-test-scripts/`; request/expected-response JSON fixtures live under `src/test/resources/`. Object Mothers (`*Mother`) build test objects; `@WithMockDatabaseUser` sets up an authenticated user.
- PMD requires JUnit 5 test classes/methods to be package-private.

## Design docs

Feature designs and implementation plans live in `docs/superpowers/specs/` and `docs/superpowers/plans/` (dated filenames); check for an existing spec/plan before implementing a feature. `TODO.md` tracks outstanding work.
