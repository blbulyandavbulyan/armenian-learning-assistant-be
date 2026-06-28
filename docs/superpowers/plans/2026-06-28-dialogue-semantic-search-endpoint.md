# Dialogue Semantic Search Endpoint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a semantic search endpoint for dialogues that returns the top 50 summaries based on vector similarity, utilizing caching to minimize database and AI model load.

**Architecture:** We will introduce a new caching configuration, and implement a new `DialogueSearchService` that coordinates query normalization, vectorization (using `EmbeddingModel`), and a repository query using `pgvector` distance operators, returning the top 50 results without pagination.

**Tech Stack:** Java 25, Spring Boot 4.0.6, Spring AI, PostgreSQL (pgvector), Caffeine Cache.

## Global Constraints
- External API calls (like Gemini embeddings) MUST NOT be inside `@Transactional` methods.
- Use `@EntityGraph` to fetch lazy collections efficiently instead of manual initialization.
- All schema changes MUST be done via Flyway migrations (not applicable here, but good to remember).
- Do not add speculative features. Leave existing code as is unless modifying for this task.
- Tests must use `ChatClient` mocking where applicable, or `EmbeddingModel` mocking in this case.
- Inspect SQL logs to verify efficient querying.

---

### Task 1: Add Dependencies and Caching Configuration

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/com/blbulyandavbulyan/larm/config/CacheConfig.java`
- Modify: `src/main/resources/application.yaml`

**Interfaces:**
- Produces: Application with enabled Caffeine caching configured with maximum size of 100 entries.

- [x] **Step 1: Add caching dependencies**
Modify `pom.xml` to include `spring-boot-starter-cache` and `caffeine`.
- [x] **Step 2: Enable Caching**
Create `CacheConfig.java` with `@EnableCaching`.
- [x] **Step 3: Configure Caffeine in properties**
Modify `application.yaml` to add `spring.cache`.
- [x] **Step 4: Clear Cache in BaseIT**
Modify `BaseIT.java` to clear the cache in `@AfterEach`.
- [x] **Step 5: Commit**

---

### Task 2: Refactor PageRequest (Completed)
- [x] Moved `PageRequest` and `PageResponse` to `api.common`.
*(Note: We won't use it for semantic search anymore, but it's used elsewhere).*

---

### Task 3: Dialogue Repository & DTOs

**Files:**
- Create: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueSummaryResponse.java`
- Create: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/SearchDialoguesResponse.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/repository/DialogueRepository.java`

**Interfaces:**
- Produces: `List<Dialogue> findTop50ByEmbedding(float[] embedding, Limit limit)`

- [x] **Step 1: Create Request and Response DTOs**
Create `DialogueSummaryResponse.java`:
```java
package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;
import com.blbulyandavbulyan.larm.api.phrases.PhraseResponse;

public record DialogueSummaryResponse(UUID id, PhraseResponse title) {}
```
Create `SearchDialoguesResponse.java`:
```java
package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

public record SearchDialoguesResponse(List<DialogueSummaryResponse> dialogues) {}
```

- [x] **Step 2: Update DialogueRepository**
Modify `DialogueRepository.java` to add the search method with `@EntityGraph`.
```java
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;

    @EntityGraph(attributePaths = {"title", "title.translations", "title.mediaSet"})
    @Query("""
            SELECT d FROM Dialogue d \
            ORDER BY l2_distance(d.embedding, :embedding)\
            """)
    List<Dialogue> searchByEmbedding(@Param("embedding") float[] embedding, Limit limit);
```

- [x] **Step 3: Update DialogueResponseMapper**
Add a method to map `List<Dialogue>` to `SearchDialoguesResponse`.
```java
    public SearchDialoguesResponse toSearchDialoguesResponse(List<Dialogue> results) {
        List<DialogueSummaryResponse> dialogues = results.stream()
                .map(d -> new DialogueSummaryResponse(d.getId(), phraseResponseMapper.mapToPhraseResponse(d.getTitle())))
                .toList();
        return new SearchDialoguesResponse(dialogues);
    }
```

---

### Task 4: Semantic Search Service

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dialogue/service/DialogueSearchService.java`

**Interfaces:**
- Consumes: `EmbeddingModel`, `DialogueRepository`, `DialogueResponseMapper`
- Produces: `SearchDialoguesResponse searchDialogues(String query)`

- [x] **Step 1: Write service implementation**
Update `DialogueSearchService.java` to use the new architecture. Do NOT use `@Transactional` on the method because it calls `embeddingModel`.
```java
package com.blbulyandavbulyan.larm.dialogue.service;

import com.blbulyandavbulyan.larm.api.dialogues.DialogueResponseMapper;
import com.blbulyandavbulyan.larm.api.dialogues.SearchDialoguesResponse;
import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DialogueSearchService {

    private final DialogueRepository dialogueRepository;
    private final EmbeddingModel embeddingModel;
    private final DialogueResponseMapper dialogueResponseMapper;

    @Cacheable(value = "semanticSearch", key = "#query.trim()")
    public SearchDialoguesResponse searchDialogues(String query) {
        String normalizedQuery = query.trim();
        float[] embedding = embeddingModel.embed(normalizedQuery);
        List<Dialogue> results = dialogueRepository.searchByEmbedding(embedding, Limit.of(50));
        return dialogueResponseMapper.toSearchDialoguesResponse(results);
    }
}
```

---

### Task 5: Search Endpoint Integration

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialoguesApi.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueController.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java`

**Interfaces:**
- Consumes: `DialogueSearchService`
- Produces: `GET /dialogues/search` endpoint returning top 50 results.

- [x] **Step 1: Update DialoguesApi and DialogueController**
Change the signature to `searchDialogues(@RequestParam("query") @NotBlank String query)` without `PageRequest`.
```java
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    // ... API docs ...
    SearchDialoguesResponse searchDialogues(@RequestParam("query") @NotBlank String query);
```

- [x] **Step 2: Update expected JSON responses**
Combine `search-page-1.json` and `search-page-2.json` into a single `search-success.json` that does NOT contain the `page` property.

- [x] **Step 3: Update Integration Test**
Modify `DialogueControllerIT.java` to test the new non-paginated endpoint.
Remove `pageNumber` and `pageSize` params from requests.

- [x] **Step 4: Cleanup**
Delete `DialogueSummaryResource.java` if it still exists.

- [x] **Step 5: Run tests and inspect SQL logs**
Run `mvn clean verify` and check that the SQL query has a LIMIT clause and eager joins for title, translations, and mediaSet.

- [x] **Step 6: Commit**
```bash
git add .
git commit -m "refactor: implement top 50 semantic search with EntityGraph and drop anti-pattern transaction"
```
