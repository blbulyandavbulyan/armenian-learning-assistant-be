# Vectorization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add phrase and dialogue vectorization logic using Spring AI's `EmbeddingModel` and save the vectors to the database using `pgvector`. This must be done outside of transactional boundaries (in `PhraseProcessor` and `DialogueOrchestrator`) to avoid holding DB connections open while waiting for external AI API calls.

**Architecture:** We will create two focused services (`PhraseVectorizationService` and `DialogueVectorizationService`) in the `com.blbulyandavbulyan.larm.ai.embedding` package. We will update the internal DTOs (`SavePhraseParameters` and `StoreDialogueParameters`) and Entities to hold the embedding array. Finally, we will hook the vectorization services into the orchestration layer (`PhraseProcessor` and `DialogueOrchestrator`) before the data hits the transactional saving services. **Per project rules, no unit tests are allowed; all verification will be done via Integration Tests (ITs).**

**Tech Stack:** Java 25, Spring Boot 4.0.6, Spring AI, PostgreSQL (pgvector), Hibernate.

## Global Constraints

- Do not refactor unrelated files or change configuration properties.
- **NO UNIT TESTS.** All tests must be Integration Tests (`*IT.java`).
- Mock external dependencies like `EmbeddingModel` in `BaseIT.java`.
- All code must follow Checkstyle.

---

### Task 1: Update Entities and Parameters to carry the embedding

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/Phrase.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/Dialogue.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/phrase/SavePhraseParameters.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dialogue/StoreDialogueParameters.java`

- [ ] **Step 1: Update Entities**

In both `Phrase.java` and `Dialogue.java`, add the following field:
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@JdbcTypeCode(SqlTypes.VECTOR)
private float[] embedding;
```

- [ ] **Step 2: Update Parameters (Records)**

Add `float[] embedding` to the record definitions for `SavePhraseParameters` and `StoreDialogueParameters`. Update any failing mappers (e.g. `PhraseMapper`) or tests so that the `embedding` value is passed from the Parameter to the Entity.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/blbulyandavbulyan/larm/dao/entities/ src/main/java/com/blbulyandavbulyan/larm/phrase/ src/main/java/com/blbulyandavbulyan/larm/dialogue/
git commit -m "feat: add embedding fields to entities and parameter records"
```

### Task 4: Create Vectorization Services

**Files:**
- Create: `src/main/java/com/blbulyandavbulyan/larm/ai/embedding/PhraseVectorizationService.java`
- Create: `src/main/java/com/blbulyandavbulyan/larm/ai/embedding/DialogueVectorizationService.java`

- [ ] **Step 1: Write PhraseVectorizationService**

```java
package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.blbulyandavbulyan.larm.core.CreateNewPhraseParameters;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhraseVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(CreateNewPhraseParameters params) {
        String base = params.isoLanguageCode() + ": " + params.phrase();
        String translations = "";
        if (params.translations() != null && !params.translations().isEmpty()) {
            translations = ", " + params.translations().stream()
                    .map(t -> t.isoLanguageCode() + ": " + t.translationText())
                    .collect(Collectors.joining(", "));
        }
        String comboText = base + translations;
        return embeddingModel.embed(comboText);
    }
}
```

- [ ] **Step 2: Write DialogueVectorizationService**

```java
package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.blbulyandavbulyan.larm.core.SaveDialogueParameters;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DialogueVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(SaveDialogueParameters params) {
        if (params.dialoguePhrases() == null) {
            return new float[0];
        }
        String concatenatedText = params.dialoguePhrases().stream()
                .map(dp -> {
                    String text = dp.isoLanguageCode() + ": " + dp.phrase();
                    if (dp.translations() != null && !dp.translations().isEmpty()) {
                        text += ", " + dp.translations().stream()
                                .map(t -> t.isoLanguageCode() + ": " + t.translationText())
                                .collect(Collectors.joining(", "));
                    }
                    return text;
                })
                .collect(Collectors.joining("\n"));
                
        return embeddingModel.embed(concatenatedText);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/blbulyandavbulyan/larm/ai/embedding/
git commit -m "feat: implement vectorization services"
```

### Task 5: Hook into PhraseProcessor and DialogueOrchestrator

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/core/PhraseProcessor.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/core/DialogueOrchestrator.java`

- [ ] **Step 1: PhraseProcessor**

Inject `PhraseVectorizationService`. In the `process` method, generate the vector:
`float[] embedding = phraseVectorizationService.vectorize(parameters);`
Pass `embedding` to `SavePhraseParameters.builder().embedding(embedding).build()`.

- [ ] **Step 2: DialogueOrchestrator**

Inject `DialogueVectorizationService`. In the `saveDialogue` method, generate the vector:
`float[] embedding = dialogueVectorizationService.vectorize(parameters);`
Pass `embedding` to `StoreDialogueParameters.builder().embedding(embedding).build()`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/blbulyandavbulyan/larm/core/PhraseProcessor.java src/main/java/com/blbulyandavbulyan/larm/core/DialogueOrchestrator.java
git commit -m "feat: hook vectorization into orchestrators"
```

### Task 6: Update Integration Tests

**Files:**
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/BaseIT.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/phrases/PhrasesControllerSavePhrasesIT.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java`

- [ ] **Step 1: Mock EmbeddingModel in BaseIT**

In `BaseIT.java`, add the mock:
```java
@MockitoBean
protected org.springframework.ai.embedding.EmbeddingModel embeddingModel;
```

- [ ] **Step 2: Update PhrasesControllerSavePhrasesIT**

In the setup or within the test methods for saving a phrase, stub the mock by constructing the expected combo string from the Test Mothers (e.g. using `PhraseMother.DefaultPhrase.PHRASE` and `TranslationMother.DefaultTranslation.TRANSLATION_TEXT`):

`when(embeddingModel.embed(expectedComboString)).thenReturn(new float[]{0.123f, 0.456f});`

Assert that the phrase was saved with this embedding. Use the `phraseRecordAssertHelper` or fetch directly from the repository to assert the vector matches `new float[]{0.123f, 0.456f}`.

- [ ] **Step 3: Update DialogueControllerIT**

In the setup or within the test methods for saving a dialogue, stub the mock exactly to ensure the concatenation string logic is correct, and return unique vectors for each phrase. 
**CRITICAL:** Do NOT use hardcoded strings like `"hy: Բարեւ ձեզ"`. You MUST construct the expected strings dynamically using the constants defined in the nested interfaces of `PhraseMother` and `TranslationMother` (e.g., `PhraseMother.DialoguePhrase1.ISO_LANGUAGE_CODE + ": " + PhraseMother.DialoguePhrase1.PHRASE + ", en: " + TranslationMother.DialoguePhrase1Translation.TRANSLATION_TEXT`).
And these constructed strings must be defined in the respected mother class, either `PhraseMother` or `DialogueMother`, depending where the constructed phrase belongs.

```java
// Stub for individual phrases using Mother constants
when(embeddingModel.embed(expectedPhrase1ComboString))
    .thenReturn(new float[]{0.1f, 0.2f});
when(embeddingModel.embed(expectedPhrase2ComboString))
    .thenReturn(new float[]{0.3f, 0.4f});

// Stub for full dialogue concatenation using Mother constants
when(embeddingModel.embed(expectedPhrase1ComboString + "\n" + expectedPhrase2ComboString))
    .thenReturn(new float[]{0.9f, 0.9f});
```

Assert that the dialogue and its phrases were saved with their respective unique embeddings. Use `dialogueRecordAssertHelper` or fetch directly to verify the vectors match the exact unique values returned above.

- [ ] **Step 4: Verify and Commit**

Run: `mvn clean test`

```bash
git add src/test/
git commit -m "test: update ITs to verify vectorization"
```
