# Endpoint Caching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement HTTP client-side caching for assets and dialogues endpoints, and server-side memory caching for dialogue lookups.

**Architecture:** Use Spring's `ResponseEntity` and `CacheControl` builder for HTTP headers, and Spring Cache (`@Cacheable`) for server-side memory caching of dialogue queries.

**Tech Stack:** Java 25, Spring Boot, Spring Cache

## Global Constraints
- `GET /dialogues/{id}`: Cache for 1 week on the client/proxy via HTTP `Cache-Control` header. Cache server-side.
- `GET /assets/{id}`: Cache for 1 week on the client/proxy via HTTP `Cache-Control` header. Do NOT cache server-side.
- Do NOT skip TDD. Watch tests fail before writing implementation code.

---

### Task 1: HTTP Cache-Control for Assets

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/assets/AssetsController.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/assets/AssetsControllerIT.java`

**Interfaces:**
- Consumes: N/A
- Produces: `ResponseEntity<Resource>` with `Cache-Control: max-age=604800, public` header.

- [ ] **Step 1: Write the failing test**

Modify `src/test/java/com/blbulyandavbulyan/larm/api/assets/AssetsControllerIT.java` by adding an assertion for the Cache-Control header in an existing test (e.g., the success case).

```java
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

// Inside the test that expects status().isOk(), add:
.andExpect(header().string("Cache-Control", "max-age=604800, public"))
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn verify -Dit.test=AssetsControllerIT`
Expected: FAIL with "Response header 'Cache-Control' expected:<max-age=604800, public> but was:<null>"

- [ ] **Step 3: Write minimal implementation**

Modify `src/main/java/com/blbulyandavbulyan/larm/api/assets/AssetsController.java`. Update `getAsset` method to set cache control.

```java
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
// ...
    @Override
    public ResponseEntity<Resource> getAsset(UUID mediaId) {
        AssetResource asset = assetService.findAssetByMediaId(mediaId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(asset.fileName()))
                .body(asset.resource());
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn verify -Dit.test=AssetsControllerIT`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/blbulyandavbulyan/larm/api/assets/AssetsControllerIT.java src/main/java/com/blbulyandavbulyan/larm/api/assets/AssetsController.java
git commit -m "feat: add cache-control header for assets endpoint"
```

### Task 2: HTTP Cache-Control for Dialogues

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialoguesApi.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueController.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java`

**Interfaces:**
- Consumes: N/A
- Produces: `ResponseEntity<GetDialogueResponse>` with `Cache-Control: max-age=604800, public` header.

- [ ] **Step 1: Write the failing test**

Modify `src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java`. Find the `getDialogue()` test method and add the header assertion.

```java
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

// In getDialogue() test:
        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=604800, public"))
                .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn verify -Dit.test=DialogueControllerIT#getDialogue`
Expected: FAIL with missing Cache-Control header.

- [ ] **Step 3: Write minimal implementation**

Modify `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialoguesApi.java`:
```java
import org.springframework.http.ResponseEntity;
// ...
    ResponseEntity<GetDialogueResponse> getDialogue(@PathVariable("id") UUID id);
```

Modify `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueController.java`:
```java
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
// ...
    @Override
    public ResponseEntity<GetDialogueResponse> getDialogue(UUID id) {
        GetDialogueResponse response = dialogueRetrievalService.getDialogue(id)
                .map(dialogueResponseMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dialogue not found"));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(response);
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn verify -Dit.test=DialogueControllerIT#getDialogue`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialoguesApi.java src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueController.java
git commit -m "feat: add cache-control header for dialogues endpoint"
```

### Task 3: Server-Side Caching for Dialogues

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dialogue/DialogueRetrievalService.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java`

**Interfaces:**
- Consumes: `@Cacheable` annotation from Spring
- Produces: Server-side cached method executions

- [ ] **Step 1: Write the failing test**

Modify `src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java` to test caching.

```java
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;

// Add field to IT class if missing:
    @MockitoSpyBean
    private DialogueRepository dialogueRepository;

// Add new test method:
    @Test
    @Sql("/sql-test-scripts/insert-dialogue.sql")
    void getDialogue_cachesResults() throws Exception {
        UUID dialogueId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        // First call
        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk());

        // Second call
        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk());

        // Verify repository was hit only once due to caching
        verify(dialogueRepository, times(1)).findByIdEagerly(dialogueId);
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn verify -Dit.test=DialogueControllerIT#getDialogue_cachesResults`
Expected: FAIL, repository called 2 times instead of 1.

- [ ] **Step 3: Write minimal implementation**

Modify `src/main/java/com/blbulyandavbulyan/larm/dialogue/DialogueRetrievalService.java`:
```java
import org.springframework.cache.annotation.Cacheable;
// ...
    @Cacheable("dialogues")
    public Optional<Dialogue> getDialogue(UUID id) {
        return dialogueRepository.findByIdEagerly(id);
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn verify -Dit.test=DialogueControllerIT#getDialogue_cachesResults`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java src/main/java/com/blbulyandavbulyan/larm/dialogue/DialogueRetrievalService.java
git commit -m "feat: add server-side caching for dialogue retrieval"
```
