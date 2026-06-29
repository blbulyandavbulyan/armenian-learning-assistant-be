# Drop Standalone Phrase Endpoint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the standalone `POST /phrases` endpoint and phrase vectorization to reduce Gemini API calls and prepare the system for dialogue-centric learning.

**Architecture:** We are dropping the `embedding` column from the `Phrase` entity (and the DB) and deleting the unused `PhraseVectorizationService` and `PhrasesApi#savePhrases` endpoint. 

**Tech Stack:** Java 25, Spring Boot, Spring Data JPA, Flyway, PostgreSQL.

## Global Constraints

- Must follow Spring Modulith principles (keep boundaries clean).
- Always use `mvn verify` instead of `mvn test`.
- Mock external dependencies (`ChatClient`, etc.) only at the boundary in IT tests.
- Flyway migrations must be used for schema changes.

---

### Task 1: Create Flyway Migration and Update Entities

**Files:**
- Create: `src/main/resources/db/migration/V1.2__Remove_Phrase_Embedding.sql`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/Phrase.java`

**Interfaces:**
- Produces: Updated database schema and Phrase entity without `embedding`.

- [ ] **Step 1: Write Flyway Migration**

Create `src/main/resources/db/migration/V1.2__Remove_Phrase_Embedding.sql`:
```sql
-- Remove vector embedding from phrases to save storage and API cost, as semantic search will be dialogue-level
ALTER TABLE phrases DROP COLUMN embedding;
```

- [ ] **Step 2: Remove embedding from Phrase**

In `Phrase.java`, delete the `float[] embedding` field.

- [ ] **Step 3: Run build to verify basic compilation**

Run: `mvn clean compile`
Expected: PASS (There will be compile errors in services, which we'll fix in subsequent tasks, but entities should compile)

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/blbulyandavbulyan/larm/dao/entities/ src/main/resources/db/migration/
git commit -m "feat: Remove phrase embedding from DB and entity"
```

---

### Task 2: Remove PhraseVectorizationService and clean API

**Files:**
- Delete: `src/main/java/com/blbulyandavbulyan/larm/ai/embedding/PhraseVectorizationService.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/core/PhraseProcessor.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/phrase/SavePhraseParameters.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/phrases/PhrasesApi.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/phrases/PhraseController.java`
- Delete: `src/main/java/com/blbulyandavbulyan/larm/core/PhraseOrchestrator.java`

**Interfaces:**
- Produces: Simplified `SavePhraseParameters` without `embedding`.

- [ ] **Step 1: Delete PhraseVectorizationService and clean PhraseProcessor**

Delete `PhraseVectorizationService.java`.
In `PhraseProcessor.java`, remove `phraseVectorizationService` dependency.
Remove `embedding` assignment when building `SavePhraseParameters`.

- [ ] **Step 2: Clean SavePhraseParameters**

In `SavePhraseParameters.java`, remove `float[] embedding`.

- [ ] **Step 3: Remove POST endpoint and PhraseOrchestrator**

In `PhrasesApi.java`, remove the `@PostMapping savePhrases(...)` method.
In `PhraseController.java` (if it exists, implementing the API), remove the implementation.
Delete `PhraseOrchestrator.java`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/
git commit -m "refactor: Remove phrase vectorization and unused phrases POST endpoint"
```

---

### Task 3: Fix and Run Integration Tests

**Files:**
- Delete: `src/test/java/com/blbulyandavbulyan/larm/core/PhraseOrchestratorIT.java` (or similar tests for the deleted class)
- Modify: `src/test/java/com/blbulyandavbulyan/larm/...` (Tests affected by the removal of `savePhrases`)

**Interfaces:**
- N/A

- [ ] **Step 1: Fix broken tests**

Identify any test failures from the structural changes. Remove tests that explicitly tested the `savePhrases` endpoint. Remove tests for `PhraseVectorizationService` and `PhraseOrchestrator`.

- [ ] **Step 2: Run all tests**

Run: `mvn clean verify`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/
git commit -m "test: Remove tests for deleted phrase vectorization and endpoints"
```
