# Spaced Repetition Exercise Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the spaced-repetition exercise backend from
`docs/superpowers/specs/2026-08-14-spaced-repetition-exercise-mode-design.md`. That covers enrolling
dialogues, one SM-2 card per (exercise type, speaker), TYPING sessions with server-side evaluation
of each answer, a due queue, and history.

**Architecture:** Entities, enums and repositories go into the existing `dao.entities` /
`dao.repository` packages. Services live in `learning` (enrolment, the `ContentItemResolver` plug-in
point) and `exercise` (sessions, evaluation, SM-2). They return records built inside the
transaction. HTTP lives in `api.learning` and `api.exercises`. `dialogue` implements the resolver
for dialogues. Answers are polymorphic (`TEXT` today, `AUDIO` later), and evaluators are chosen
by the card's exercise type.

**Tech Stack:** Java 25, Spring Boot 4, Spring Data JPA (Hibernate 7), PostgreSQL + pgvector
(Testcontainers in tests), Flyway, Lombok, Jackson 3, AssertJ, MockMvc.

## Global Constraints

- **Layout:** entities and enums → `dao.entities`; repositories → `dao.repository` (package is
  `@NullMarked`: optional repository parameters MUST be annotated `@org.jspecify.annotations.Nullable`);
  services → `learning` / `exercise`; controllers, DTOs, `*Api` interfaces, mappers → `api.*`.
  `api` may depend on everything; nothing outside `api` may depend on `api` (`ArchitectureTest`).
- **No lazy entity ever leaves a service.** `open-in-view` is `false`; services map entities to
  records inside `@Transactional` methods.
- **Time** always comes from the injected `java.time.Clock` (`Instant.now(clock)`), never `Instant.now()`.
- **Enums** are `VARCHAR(32)` + named `CHECK` constraint, mapped with `@Enumerated(EnumType.STRING)`.
- **Errors:** not found / foreign → `core.exception.EntityNotFoundException` (404, already mapped);
  state conflicts → new `core.exception.ConflictException` (409).
- **Auth parameter:** `*Api` methods take `DatabaseUserJwtAuthenticationToken auth` as their first
  parameter, like `ChatApi`. No `@AuthenticationPrincipal`; that resolves the `Jwt` principal,
  not the token.
- **Checkstyle runs on main and test sources during `compile` and fails the build.** Every snippet
  below follows it. Keep it that way when adapting code:
  - imports: `java.*`, then `javax.*`, then the rest alphabetically, then `static`, with a blank
    line between groups and no star imports
  - braces on every `if`
  - one variable per declaration
  - lines of at most 150 characters
  - Javadoc summaries start with a capital letter and end with a period
- **PMD:** JUnit test classes and methods are package-private.
- **Integration tests** extend `BaseIT`. State comes from SQL fixtures; each test calls **only the
  endpoint under test**. Responses are compared in full against JSON files (`JsonCompareMode.STRICT`).
  Mutating calls also assert DB rows. Test names follow `endpoint_condition`, like
  `DialogueControllerIT` (`saveDialogue_withInvalidFields`).
- Run integration tests with `mvn verify`, never `mvn test` alone.
- Next migration version: **V4**.

## Shared Test Data

All SRS tests reuse `insert-dialogue.sql` (dialogue `99999999-9999-9999-9999-999999999999`):

| line (`dialogue_phrases.id`) | order | speaker | text |
|---|---|---|---|
| `cccccccc-cccc-cccc-cccc-cccccccccccc` | 0 | A `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | `Բարեւ ձեզ` |
| `dddddddd-dddd-dddd-dddd-dddddddddddd` | 1 | B `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb` | `Բարեւ ձեզ, խնդրում եմ մեկ հաց:` |
| `eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee` | 2 | A | `Ահա, խնդրեմ:` |

New ids (all fixtures, all tests):

| what | id |
|---|---|
| test user (`@WithMockDatabaseUser` defaults) | `5a5a5a5a-0000-0000-0000-000000000001` |
| other user | `5a5a5a5a-0000-0000-0000-000000000002` |
| learning item (test user) | `1a1a1a1a-0000-0000-0000-000000000001` |
| learning item (other user) | `1a1a1a1a-0000-0000-0000-000000000002` |
| card speaker A / B (test user) | `2b2b2b2b-0000-0000-0000-0000000000aa` / `2b2b2b2b-0000-0000-0000-0000000000bb` |
| card speaker A / B (other user) | `2b2b2b2b-0000-0000-0000-0000000002aa` / `2b2b2b2b-0000-0000-0000-0000000002bb` |
| in-progress session (card A) | `3c3c3c3c-0000-0000-0000-000000000001` |
| completed session (card B) | `3c3c3c3c-0000-0000-0000-000000000002` |
| other user's in-progress session (their card A) | `3c3c3c3c-0000-0000-0000-000000000003` |
| attempts | `4d4d4d4d-0000-0000-0000-00000000000{1,2,3,4}` |

The fixed clock in `BaseIT` is **`2026-08-15T10:00:00Z`**.

---

## File Map

**Create — main** (`src/main/java/com/blbulyandavbulyan/larm/` prefix omitted):

| file | purpose |
|---|---|
| `config/ClockConfig.java` | `Clock` bean |
| `core/exception/ConflictException.java` | 409 |
| `dao/entities/ContentItemType.java`, `ExerciseType.java`, `AnswerType.java` | enums |
| `dao/entities/LearningItem.java`, `LearningCard.java`, `ExerciseSession.java`, `ExerciseAttempt.java` | entities |
| `dao/repository/LearningItemRepository.java`, `LearningCardRepository.java`, `ExerciseSessionRepository.java`, `ExerciseAttemptRepository.java` | repositories |
| `dao/repository/DialoguePhraseRepository.java`, `DialogueSpeakerRepository.java` | resolver queries |
| `learning/ContentItemResolver.java`, `ContentItemResolvers.java`, `ExpectedAnswer.java` | content plug-in point |
| `learning/LearningItemService.java`, `LearningItemResource.java`, `LearningCardResource.java`, `EnrolmentResult.java` | enrolment |
| `dialogue/DialogueContentItemResolver.java` | resolver for dialogues |
| `exercise/srs/Sm2Algorithm.java`, `Sm2Input.java`, `Sm2Result.java` | SM-2 |
| `exercise/Answer.java`, `TextAnswer.java` | domain answers |
| `exercise/evaluation/AnswerEvaluator.java`, `AnswerEvaluators.java`, `Evaluation.java`, `TypingAnswerEvaluator.java`, `UnsupportedAnswerTypeException.java` | evaluation |
| `exercise/AttemptCommand.java`, `AttemptResultResource.java`, `AttemptResource.java`, `SessionResource.java`, `SessionStartResult.java`, `SessionStatusResource.java`, `SubItemAttemptsResource.java`, `CompletionResource.java`, `DueCardResource.java`, `HistoryItemResource.java` | records |
| `exercise/ExerciseSessionService.java`, `ExerciseQueueService.java`, `ExerciseHistoryService.java` | services |
| `api/learning/*` | `LearningItemsApi`, `LearningItemsController`, `LearningResponseMapper`, `EnrolLearningItemRequest`, `LearningItemResponse`, `LearningCardsResponse` |
| `api/exercises/*` | `ExercisesApi`, `ExercisesController`, `ExerciseResponseMapper`, `StartSessionRequest`, `AttemptRequest`, `AnswerRequest`, `TextAnswerRequest`, `SessionResponse`, `AttemptResponse`, `SessionStatusResponse`, `CompleteSessionResponse`, `QueueResponse`, `HistoryResponse` |
| `src/main/resources/db/migration/V4__create_srs_tables.sql` | schema |

**Modify:** `api/dialogues/GetDialogueResponse.java`, `api/dialogues/DialogueResponseMapper.java`,
`api/openapi/OpenApiConstants.java`, `api/advice/GlobalControllerAdvice.java`,
`src/test/java/.../BaseIT.java`, `src/test/java/.../dao/entities/JpaEntitiesIT.java`,
`src/test/resources/responses/get-dialogue-response.json`,
`src/test/resources/sql-test-scripts/drop-all-data-after-test.sql`.

**Create — tests:** `exercise/srs/Sm2AlgorithmTest`, `exercise/evaluation/TypingAnswerEvaluatorTest`,
`api/learning/LearningItemsControllerIT`, `api/exercises/ExerciseSessionControllerIT`,
`api/exercises/ExerciseQueueControllerIT`, `api/exercises/ExerciseHistoryControllerIT`; SQL fixtures
under `src/test/resources/sql-test-scripts/srs/`; expected JSON under
`src/test/resources/expected-responses/srs/`.

---

## Task 1: Expose the Dialogue Line Id

Sessions refer to lines by `dialogue_phrases.id`, which `GET /dialogues/{id}` does not return yet.
`phrase.id` is not a substitute: phrases are deduplicated, so a phrase repeated in one dialogue
has one id.

**Files:** modify `api/dialogues/GetDialogueResponse.java`, `api/dialogues/DialogueResponseMapper.java`,
`api/openapi/OpenApiConstants.java`, `src/test/resources/responses/get-dialogue-response.json`.

- [ ] **Step 1: Add the description constant** to `OpenApiConstants.Descriptions` (after `DIALOGUE_PHRASES`):

```java
        String DIALOGUE_PHRASE_ID = "Id of this line of the dialogue (dialogue phrase), used as sub-item id in exercises";
```

- [ ] **Step 2: Add the field** to `GetDialogueResponse.DialoguePhraseResponse` as its first component:

```java
    public record DialoguePhraseResponse(
            @Schema(description = Descriptions.DIALOGUE_PHRASE_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID id,

            @Schema(description = Descriptions.SPEAKER_ID, examples = Examples.SPEAKER_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID speakerId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            PhraseResponse phrase) {
    }
```

- [ ] **Step 3: Map it** in `DialogueResponseMapper.mapDialoguePhrases`:

```java
                .map(dialoguePhrase -> GetDialogueResponse.DialoguePhraseResponse.builder()
                        .id(dialoguePhrase.getId())
                        .speakerId(dialoguePhrase.getSpeaker().getId())
```

- [ ] **Step 4: Update the expected response.** In `src/test/resources/responses/get-dialogue-response.json`,
add `"id"` as the first key of each `dialoguePhrases` entry: `cccccccc-cccc-cccc-cccc-cccccccccccc` for
`Բարեւ ձեզ`, `dddddddd-dddd-dddd-dddd-dddddddddddd` for `Բարեւ ձեզ, խնդրում եմ մեկ հաց:`,
`eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee` for `Ահա, խնդրեմ:`.

- [ ] **Step 5: Run** — `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=DialogueControllerIT` → all pass.

- [ ] **Step 6: Commit** — `git commit -am "feat(dialogues): expose dialogue phrase id in GET /dialogues/{id}"`

---

## Task 2: Flyway Migration

**Files:** create `src/main/resources/db/migration/V4__create_srs_tables.sql`; modify
`src/test/resources/sql-test-scripts/drop-all-data-after-test.sql`.

- [ ] **Step 1: Create the migration**

```sql
CREATE TABLE learning_items
(
    id                UUID PRIMARY KEY,
    user_id           UUID                     NOT NULL REFERENCES users (id),
    -- soft reference: points to dialogues.id today, to other content tables later
    content_item_id   UUID                     NOT NULL,
    content_item_type VARCHAR(32)              NOT NULL,
    enrolled_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    excluded_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_learning_items_content_item_type CHECK (content_item_type IN ('DIALOGUE')),
    CONSTRAINT uq_learning_items_user_content UNIQUE (user_id, content_item_id, content_item_type)
);

CREATE TABLE learning_cards
(
    id               UUID PRIMARY KEY,
    learning_item_id UUID                     NOT NULL REFERENCES learning_items (id),
    exercise_type    VARCHAR(32)              NOT NULL,
    -- DIALOGUE: dialogue_speakers.id (the role the user plays); NULL for content without variants
    variant_id       UUID,
    next_review_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    interval_days    INTEGER                  NOT NULL DEFAULT 0 CHECK (interval_days >= 0),
    ease_factor      DOUBLE PRECISION         NOT NULL DEFAULT 2.5 CHECK (ease_factor >= 1.3),
    repetition_count INTEGER                  NOT NULL DEFAULT 0 CHECK (repetition_count >= 0),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_learning_cards_exercise_type CHECK (exercise_type IN ('TYPING')),
    CONSTRAINT uq_learning_cards_item_exercise_variant UNIQUE NULLS NOT DISTINCT (learning_item_id, exercise_type, variant_id)
);

CREATE TABLE exercise_sessions
(
    id               UUID PRIMARY KEY,
    learning_card_id UUID                     NOT NULL REFERENCES learning_cards (id),
    user_id          UUID                     NOT NULL REFERENCES users (id),
    aggregate_score  INTEGER CHECK (aggregate_score BETWEEN 0 AND 100),
    started_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at     TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_exercise_sessions_completed_has_score CHECK ((completed_at IS NULL) = (aggregate_score IS NULL))
);

-- at most one open session per card
CREATE UNIQUE INDEX uq_exercise_sessions_one_in_progress
    ON exercise_sessions (learning_card_id)
    WHERE completed_at IS NULL;

CREATE TABLE exercise_attempts
(
    id                  UUID PRIMARY KEY,
    session_id          UUID                     NOT NULL REFERENCES exercise_sessions (id),
    content_sub_item_id UUID                     NOT NULL,
    answer_type         VARCHAR(32)              NOT NULL,
    answer_text         TEXT,
    used_hint           BOOLEAN                  NOT NULL,
    raw_quality         INTEGER                  NOT NULL CHECK (raw_quality BETWEEN 0 AND 5),
    quality             INTEGER                  NOT NULL CHECK (quality BETWEEN 0 AND 5),
    correct             BOOLEAN                  NOT NULL,
    evaluated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_exercise_attempts_answer_type CHECK (answer_type IN ('TEXT')),
    CONSTRAINT chk_exercise_attempts_text_answer CHECK (answer_type <> 'TEXT' OR answer_text IS NOT NULL)
);

CREATE INDEX idx_learning_cards_item_review ON learning_cards (learning_item_id, next_review_at);
CREATE INDEX idx_exercise_sessions_user_completed ON exercise_sessions (user_id, completed_at);
CREATE INDEX idx_exercise_attempts_session ON exercise_attempts (session_id);
```

- [ ] **Step 2: Update `drop-all-data-after-test.sql`** (children first):

```sql
TRUNCATE TABLE exercise_attempts CASCADE;
TRUNCATE TABLE exercise_sessions CASCADE;
TRUNCATE TABLE learning_cards CASCADE;
TRUNCATE TABLE learning_items CASCADE;
TRUNCATE TABLE dialogues CASCADE;
TRUNCATE TABLE phrases CASCADE;
TRUNCATE TABLE users CASCADE;
```

- [ ] **Step 3: Verify the migration applies** — `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=JpaEntitiesIT`
→ passes (Flyway runs V4 against Testcontainers Postgres 17).

- [ ] **Step 4: Commit** — `git commit -m "feat(db): add V4 migration for spaced repetition tables"`

---

## Task 3: Clock and Conflict Exception

**Files:** create `config/ClockConfig.java`, `core/exception/ConflictException.java`; modify
`api/advice/GlobalControllerAdvice.java`, `src/test/java/com/blbulyandavbulyan/larm/BaseIT.java`.

- [ ] **Step 1: `ClockConfig.java`**

```java
package com.blbulyandavbulyan.larm.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
```

- [ ] **Step 2: `ConflictException.java`**

```java
package com.blbulyandavbulyan.larm.core.exception;

/**
 * Thrown when a request is valid but conflicts with the current state of a resource (HTTP 409).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Map it** — add to `GlobalControllerAdvice` (keep the existing handler; add the import
`com.blbulyandavbulyan.larm.core.exception.ConflictException`):

```java
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleConflictException(ConflictException e) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage())).build();
    }
```

- [ ] **Step 4: Fix the clock in `BaseIT`.** Add the imports `java.time.Clock`, `java.time.Instant`,
`java.time.ZoneOffset` and `static org.mockito.Mockito.when`, plus:

```java
    protected static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");

    @MockitoBean
    protected Clock clock;
```

and at the start of `beforeEach`:

```java
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
```

Also add a strict JSON comparison helper (import `org.springframework.test.json.JsonAssert` and
`org.springframework.test.json.JsonCompareMode`). It is for responses that contain a server-generated
id, which is only known after the call:

```java
    protected static void assertJsonEquals(String expectedJson, String actualJson) {
        JsonAssert.comparator(JsonCompareMode.STRICT).assertIsMatch(expectedJson, actualJson);
    }
```

- [ ] **Step 5: Run all tests** — `mvn verify` → all pass (nothing uses the clock yet).

- [ ] **Step 6: Commit** — `git commit -m "feat: add Clock bean, ConflictException and fixed test clock"`

---

## Task 4: Enums, Entities, Repositories

**Files:** create the 3 enums, 4 entities, 6 repositories listed in the File Map.

- [ ] **Step 1: Enums** (`dao.entities`, next to `StorageProvider`)

```java
package com.blbulyandavbulyan.larm.dao.entities;

public enum ContentItemType {
    DIALOGUE
}
```

```java
package com.blbulyandavbulyan.larm.dao.entities;

public enum ExerciseType {
    TYPING
}
```

```java
package com.blbulyandavbulyan.larm.dao.entities;

public enum AnswerType {
    TEXT
}
```

- [ ] **Step 2: `LearningItem.java`**

```java
package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "learning_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content_item_id", nullable = false)
    private UUID contentItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_item_type", nullable = false)
    private ContentItemType contentItemType;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "excluded_at")
    private Instant excludedAt;

    public boolean isExcluded() {
        return excludedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearningItem that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "LearningItem".hashCode();
    }

    @Override
    public String toString() {
        return "LearningItem{id=%s, userId=%s, contentItemId=%s, contentItemType=%s, enrolledAt=%s, excludedAt=%s}"
                .formatted(id, userId, contentItemId, contentItemType, enrolledAt, excludedAt);
    }
}
```

`userId` is a plain column, not a `@ManyToOne User`. Nothing navigates to the user, and this
avoids building fake `User` references (`User.builder().id(..)`) in the services.

- [ ] **Step 3: `LearningCard.java`**

```java
package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "learning_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_item_id", nullable = false)
    private LearningItem learningItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "next_review_at", nullable = false)
    private Instant nextReviewAt;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "ease_factor", nullable = false)
    private double easeFactor;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearningCard that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "LearningCard".hashCode();
    }

    @Override
    public String toString() {
        return ("LearningCard{id=%s, learningItem=%s, exerciseType=%s, variantId=%s, nextReviewAt=%s, intervalDays=%d, "
                + "easeFactor=%s, repetitionCount=%d, createdAt=%s, updatedAt=%s}")
                .formatted(id, LAZY_LOADING, exerciseType, variantId, nextReviewAt, intervalDays,
                        easeFactor, repetitionCount, createdAt, updatedAt);
    }
}
```

- [ ] **Step 4: `ExerciseSession.java`**

```java
package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "exercise_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_card_id", nullable = false)
    private LearningCard learningCard;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "aggregate_score")
    private Integer aggregateScore;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public boolean isCompleted() {
        return completedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExerciseSession that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "ExerciseSession".hashCode();
    }

    @Override
    public String toString() {
        return "ExerciseSession{id=%s, learningCard=%s, userId=%s, aggregateScore=%s, startedAt=%s, completedAt=%s}"
                .formatted(id, LAZY_LOADING, userId, aggregateScore, startedAt, completedAt);
    }
}
```

- [ ] **Step 5: `ExerciseAttempt.java`**

```java
package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "exercise_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ExerciseSession session;

    @Column(name = "content_sub_item_id", nullable = false)
    private UUID contentSubItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;

    @Column(name = "answer_text")
    private String answerText;

    @Column(name = "used_hint", nullable = false)
    private boolean usedHint;

    @Column(name = "raw_quality", nullable = false)
    private int rawQuality;

    @Column(name = "quality", nullable = false)
    private int quality;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExerciseAttempt that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "ExerciseAttempt".hashCode();
    }

    @Override
    public String toString() {
        return ("ExerciseAttempt{id=%s, session=%s, contentSubItemId=%s, answerType=%s, answerText=%s, usedHint=%s, "
                + "rawQuality=%d, quality=%d, correct=%s, evaluatedAt=%s}")
                .formatted(id, LAZY_LOADING, contentSubItemId, answerType, answerText, usedHint,
                        rawQuality, quality, correct, evaluatedAt);
    }
}
```

- [ ] **Step 6: `LearningItemRepository.java`**

```java
package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningItemRepository extends JpaRepository<LearningItem, UUID> {

    Optional<LearningItem> findByUserIdAndContentItemIdAndContentItemType(
            UUID userId, UUID contentItemId, ContentItemType contentItemType);

    Optional<LearningItem> findByIdAndUserId(UUID id, UUID userId);
}
```

- [ ] **Step 7: `LearningCardRepository.java`**

The filters are optional. `@Nullable` is required because the package is `@NullMarked`, and
Spring Data rejects null arguments for parameters that are not marked nullable.

```java
package com.blbulyandavbulyan.larm.dao.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningCardRepository extends JpaRepository<LearningCard, UUID> {

    List<LearningCard> findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(UUID learningItemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM LearningCard c JOIN FETCH c.learningItem i WHERE c.id = :id AND i.userId = :userId")
    Optional<LearningCard> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = "learningItem")
    @Query(value = """
            SELECT c FROM LearningCard c JOIN c.learningItem i
            WHERE i.userId = :userId
              AND i.excludedAt IS NULL
              AND c.nextReviewAt <= :now
              AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
              AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
            ORDER BY c.nextReviewAt ASC, c.id ASC
            """,
            countQuery = """
            SELECT COUNT(c) FROM LearningCard c JOIN c.learningItem i
            WHERE i.userId = :userId
              AND i.excludedAt IS NULL
              AND c.nextReviewAt <= :now
              AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
              AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
            """)
    Page<LearningCard> findDueCards(@Param("userId") UUID userId,
                                    @Param("now") Instant now,
                                    @Param("exerciseType") @Nullable ExerciseType exerciseType,
                                    @Param("contentItemType") @Nullable ContentItemType contentItemType,
                                    Pageable pageable);
}
```

> If Hibernate cannot infer the type of a null enum parameter in `:x IS NULL` (an error like
> "could not determine data type of parameter"), replace these two queries with a
> `JpaSpecificationExecutor` and build the predicates conditionally. `ExerciseQueueControllerIT`
> calls the endpoint without filters, so this cannot slip through.

- [ ] **Step 8: `ExerciseSessionRepository.java`**

```java
package com.blbulyandavbulyan.larm.dao.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseSession;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, UUID> {

    @EntityGraph(attributePaths = {"learningCard", "learningCard.learningItem"})
    Optional<ExerciseSession> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s FROM ExerciseSession s
            JOIN FETCH s.learningCard c
            JOIN FETCH c.learningItem
            WHERE s.id = :id AND s.userId = :userId
            """)
    Optional<ExerciseSession> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<ExerciseSession> findByLearningCardIdAndCompletedAtIsNull(UUID learningCardId);

    @EntityGraph(attributePaths = {"learningCard", "learningCard.learningItem"})
    @Query(value = """
            SELECT s FROM ExerciseSession s JOIN s.learningCard c JOIN c.learningItem i
            WHERE s.userId = :userId
              AND s.completedAt IS NOT NULL
              AND (CAST(:from AS java.time.Instant) IS NULL OR s.completedAt >= :from)
              AND (CAST(:to AS java.time.Instant) IS NULL OR s.completedAt <= :to)
              AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
              AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
            ORDER BY s.completedAt DESC, s.id ASC
            """,
            countQuery = """
            SELECT COUNT(s) FROM ExerciseSession s JOIN s.learningCard c JOIN c.learningItem i
            WHERE s.userId = :userId
              AND s.completedAt IS NOT NULL
              AND (CAST(:from AS java.time.Instant) IS NULL OR s.completedAt >= :from)
              AND (CAST(:to AS java.time.Instant) IS NULL OR s.completedAt <= :to)
              AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
              AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
            """)
    Page<ExerciseSession> findCompleted(@Param("userId") UUID userId,
                                        @Param("from") @Nullable Instant from,
                                        @Param("to") @Nullable Instant to,
                                        @Param("exerciseType") @Nullable ExerciseType exerciseType,
                                        @Param("contentItemType") @Nullable ContentItemType contentItemType,
                                        Pageable pageable);
}
```

> The `CAST(... AS java.time.Instant)` is the usual workaround for PostgreSQL failing to infer the
> type of a null timestamp parameter. The same fallback note as Step 7 applies.

- [ ] **Step 9: `ExerciseAttemptRepository.java`**

```java
package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, UUID> {

    List<ExerciseAttempt> findBySessionIdOrderByEvaluatedAtAscIdAsc(UUID sessionId);
}
```

- [ ] **Step 10: Resolver queries** — `DialogueSpeakerRepository.java` and `DialoguePhraseRepository.java`

```java
package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.DialogueSpeaker;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface DialogueSpeakerRepository extends Repository<DialogueSpeaker, UUID> {

    @Query("SELECT s.id FROM DialogueSpeaker s WHERE s.dialogue.id = :dialogueId ORDER BY s.createdAt ASC, s.id ASC")
    List<UUID> findIdsByDialogueId(@Param("dialogueId") UUID dialogueId);
}
```

```java
package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.DialoguePhrase;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface DialoguePhraseRepository extends Repository<DialoguePhrase, UUID> {

    @Query("SELECT dp.id FROM DialoguePhrase dp WHERE dp.dialogue.id = :dialogueId ORDER BY dp.orderIndex ASC")
    List<UUID> findIdsByDialogueId(@Param("dialogueId") UUID dialogueId);

    @Query("""
            SELECT dp.id FROM DialoguePhrase dp
            WHERE dp.dialogue.id = :dialogueId AND dp.speaker.id = :speakerId
            ORDER BY dp.orderIndex ASC
            """)
    List<UUID> findIdsByDialogueIdAndSpeakerId(@Param("dialogueId") UUID dialogueId, @Param("speakerId") UUID speakerId);

    @Query("""
            SELECT p.phrase AS text, p.transcription AS transcription
            FROM DialoguePhrase dp JOIN dp.phrase p
            WHERE dp.id = :id AND dp.dialogue.id = :dialogueId
            """)
    Optional<PhraseTextView> findPhraseText(@Param("dialogueId") UUID dialogueId, @Param("id") UUID id);

    /**
     * Read-only projection of the text of a dialogue line.
     */
    interface PhraseTextView {
        String getText();

        @Nullable
        String getTranscription();
    }
}
```

- [ ] **Step 11: Compile** — `mvn -q compile` → `BUILD SUCCESS`.

- [ ] **Step 12: Commit** — `git commit -m "feat(dao): add spaced repetition entities and repositories"`

---

## Task 5: SM-2 Algorithm

**Files:** create `exercise/srs/Sm2Input.java`, `Sm2Result.java`, `Sm2Algorithm.java`,
`src/test/java/com/blbulyandavbulyan/larm/exercise/srs/Sm2AlgorithmTest.java`.

Spec §7.2. Points that are easy to get wrong:
- The ease factor changes **only on success**.
- The score comes from the **unrounded** session mean, so it is not always a multiple of 20.
- Empty input is rejected.

- [ ] **Step 1: Write the failing test**

```java
package com.blbulyandavbulyan.larm.exercise.srs;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class Sm2AlgorithmTest {

    private final Sm2Algorithm sm2Algorithm = new Sm2Algorithm();

    private Sm2Result applySingle(int repetitionCount, int intervalDays, double easeFactor, int quality) {
        return sm2Algorithm.apply(new Sm2Input(repetitionCount, intervalDays, easeFactor, List.of(List.of(quality))));
    }

    @Test
    void apply_firstSuccessfulRepetition() {
        Sm2Result result = applySingle(0, 0, 2.5, 4);

        assertThat(result.repetitionCount()).isEqualTo(1);
        assertThat(result.intervalDays()).isEqualTo(1);
    }

    @Test
    void apply_secondSuccessfulRepetition() {
        Sm2Result result = applySingle(1, 1, 2.5, 4);

        assertThat(result.repetitionCount()).isEqualTo(2);
        assertThat(result.intervalDays()).isEqualTo(6);
    }

    @Test
    void apply_thirdSuccessfulRepetitionScalesByPreviousEaseFactor() {
        Sm2Result result = applySingle(2, 6, 2.5, 5);

        assertThat(result.repetitionCount()).isEqualTo(3);
        assertThat(result.intervalDays()).isEqualTo(15);
        assertThat(result.easeFactor()).isCloseTo(2.6, within(1e-9));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void apply_failedReviewResetsScheduleAndKeepsEaseFactor(int quality) {
        Sm2Result result = applySingle(3, 15, 2.5, quality);

        assertThat(result.repetitionCount()).isZero();
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.easeFactor()).isEqualTo(2.5);
    }

    @Test
    void apply_easeFactorNeverDropsBelowMinimum() {
        // 1.3 + 0.1 - 2 * (0.08 + 2 * 0.02) = 1.16 -> clamped
        Sm2Result result = applySingle(2, 6, 1.3, 3);

        assertThat(result.easeFactor()).isEqualTo(1.3);
    }

    @Test
    void apply_averagesRetriesPerSubItemBeforeAveragingSubItems() {
        // sub-item means 3.5 and 5 -> session mean 4.25 -> quality 4, score 85
        Sm2Result result = sm2Algorithm.apply(new Sm2Input(2, 6, 2.5, List.of(List.of(2, 5), List.of(5))));

        assertThat(result.aggregateQuality()).isEqualTo(4);
        assertThat(result.aggregateScore()).isEqualTo(85);
    }

    @Test
    void apply_perfectSessionScoresHundred() {
        assertThat(applySingle(0, 0, 2.5, 5).aggregateScore()).isEqualTo(100);
    }

    @Test
    void apply_withoutSubItems() {
        Sm2Input input = new Sm2Input(0, 0, 2.5, List.of());

        assertThatThrownBy(() -> sm2Algorithm.apply(input)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apply_withSubItemWithoutAttempts() {
        Sm2Input input = new Sm2Input(0, 0, 2.5, List.of(List.of(5), List.of()));

        assertThatThrownBy(() -> sm2Algorithm.apply(input)).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run it** — `mvn test -Dtest=Sm2AlgorithmTest` → compilation failure (classes missing).

- [ ] **Step 3: Implement**

```java
package com.blbulyandavbulyan.larm.exercise.srs;

import java.util.List;

/**
 * Card state before the review plus the attempt qualities of the session, one list per sub-item.
 */
public record Sm2Input(int repetitionCount, int intervalDays, double easeFactor, List<List<Integer>> qualitiesPerSubItem) {
}
```

```java
package com.blbulyandavbulyan.larm.exercise.srs;

/**
 * Card state after the review, the SM-2 quality used (0-5) and the session score (0-100).
 */
public record Sm2Result(int repetitionCount, int intervalDays, double easeFactor, int aggregateQuality, int aggregateScore) {
}
```

```java
package com.blbulyandavbulyan.larm.exercise.srs;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * SM-2 spaced repetition scheduling, see spec section 7.2.
 */
@Component
public class Sm2Algorithm {

    public static final double INITIAL_EASE_FACTOR = 2.5;

    private static final double MIN_EASE_FACTOR = 1.3;

    private static final int MAX_QUALITY = 5;

    private static final int PASSING_QUALITY = 3;

    public Sm2Result apply(Sm2Input input) {
        List<List<Integer>> qualitiesPerSubItem = input.qualitiesPerSubItem();
        if (qualitiesPerSubItem.isEmpty() || qualitiesPerSubItem.stream().anyMatch(List::isEmpty)) {
            throw new IllegalArgumentException("SM-2 needs at least one attempt for every sub-item");
        }
        double sessionMean = qualitiesPerSubItem.stream()
                .mapToDouble(qualities -> qualities.stream().mapToInt(Integer::intValue).average().orElseThrow())
                .average()
                .orElseThrow();
        int quality = (int) Math.round(sessionMean);
        int score = (int) Math.round(sessionMean / MAX_QUALITY * 100);

        if (quality < PASSING_QUALITY) {
            return new Sm2Result(0, 1, input.easeFactor(), quality, score);
        }
        int repetitionCount = input.repetitionCount() + 1;
        int intervalDays = switch (repetitionCount) {
            case 1 -> 1;
            case 2 -> 6;
            default -> (int) Math.round(input.intervalDays() * input.easeFactor());
        };
        int lapse = MAX_QUALITY - quality;
        double easeFactor = Math.max(MIN_EASE_FACTOR, input.easeFactor() + 0.1 - lapse * (0.08 + lapse * 0.02));
        return new Sm2Result(repetitionCount, intervalDays, easeFactor, quality, score);
    }
}
```

- [ ] **Step 4: Run it** — `mvn test -Dtest=Sm2AlgorithmTest` → all pass.

- [ ] **Step 5: Commit** — `git commit -m "feat(exercise): add SM-2 algorithm"`

---

## Task 6: Answers and Typing Evaluation

**Files:** create `exercise/Answer.java`, `exercise/TextAnswer.java`, `learning/ExpectedAnswer.java`,
`exercise/evaluation/AnswerEvaluator.java`, `Evaluation.java`, `AnswerEvaluators.java`,
`TypingAnswerEvaluator.java`, `UnsupportedAnswerTypeException.java`, the test
`exercise/evaluation/TypingAnswerEvaluatorTest.java`; modify `api/advice/GlobalControllerAdvice.java`.

This is the extension point for voice: a new `Answer` subtype plus a new evaluator, and no
changes to the session service (spec §12.1).

- [ ] **Step 1: Answers and expected answer**

```java
package com.blbulyandavbulyan.larm.exercise;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;

/**
 * An answer given by the user; one implementation per {@link AnswerType}.
 */
public sealed interface Answer permits TextAnswer {
    AnswerType type();
}
```

```java
package com.blbulyandavbulyan.larm.exercise;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;

public record TextAnswer(String text) implements Answer {
    @Override
    public AnswerType type() {
        return AnswerType.TEXT;
    }
}
```

```java
package com.blbulyandavbulyan.larm.learning;

import org.jspecify.annotations.Nullable;

/**
 * What the user is expected to answer for one sub-item; grows when new evaluators need more.
 */
public record ExpectedAnswer(String text, @Nullable String transcription) {
}
```

- [ ] **Step 2: Evaluator contract**

```java
package com.blbulyandavbulyan.larm.exercise.evaluation;

/**
 * Result of evaluating one answer: raw quality 0-5 (before hint penalty) and whether it matched exactly.
 */
public record Evaluation(int rawQuality, boolean correct) {
}
```

```java
package com.blbulyandavbulyan.larm.exercise.evaluation;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.exercise.Answer;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;

/**
 * Evaluates answers for exactly one exercise type.
 */
public interface AnswerEvaluator {

    ExerciseType supportedType();

    /**
     * Evaluates the answer against the expected answer.
     *
     * @throws UnsupportedAnswerTypeException if the answer type cannot be used for this exercise type
     */
    Evaluation evaluate(ExpectedAnswer expected, Answer answer);
}
```

```java
package com.blbulyandavbulyan.larm.exercise.evaluation;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;

/**
 * Thrown when an answer type is not suitable for the exercise type of the card (HTTP 400).
 */
public class UnsupportedAnswerTypeException extends RuntimeException {
    public UnsupportedAnswerTypeException(AnswerType answerType, ExerciseType exerciseType) {
        super("Answer type %s is not supported for exercise type %s".formatted(answerType, exerciseType));
    }
}
```

```java
package com.blbulyandavbulyan.larm.exercise.evaluation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link AnswerEvaluator} bean for an exercise type.
 */
@Component
public class AnswerEvaluators {

    private final Map<ExerciseType, AnswerEvaluator> evaluatorsByType;

    public AnswerEvaluators(List<AnswerEvaluator> evaluators) {
        // toMap fails on startup if two evaluators claim the same type
        this.evaluatorsByType = evaluators.stream()
                .collect(Collectors.toMap(AnswerEvaluator::supportedType, Function.identity()));
    }

    public AnswerEvaluator forType(ExerciseType exerciseType) {
        AnswerEvaluator evaluator = evaluatorsByType.get(exerciseType);
        if (evaluator == null) {
            throw new IllegalStateException("No answer evaluator for exercise type " + exerciseType);
        }
        return evaluator;
    }
}
```

- [ ] **Step 3: Write the failing test**

```java
package com.blbulyandavbulyan.larm.exercise.evaluation;

import com.blbulyandavbulyan.larm.exercise.TextAnswer;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypingAnswerEvaluatorTest {

    private final TypingAnswerEvaluator evaluator = new TypingAnswerEvaluator();

    private Evaluation evaluate(String expected, String typed) {
        return evaluator.evaluate(new ExpectedAnswer(expected, null), new TextAnswer(typed));
    }

    @Test
    void evaluate_exactMatch() {
        assertThat(evaluate("Բարեւ ձեզ", "Բարեւ ձեզ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_ignoresCase() {
        assertThat(evaluate("Բարեւ ձեզ", "բարեւ ձեզ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_treatsLigatureEwAsEchYiwn() {
        // U+0587 "և" is the same letters as "եւ"
        assertThat(evaluate("Բարեւ ձեզ", "Բարև ձեզ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_ignoresPunctuationIncludingArmenianQuestionMark() {
        assertThat(evaluate("Ինչպե՞ս եք:", "Ինչպես եք")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_ignoresExtraWhitespace() {
        assertThat(evaluate("Ահա, խնդրեմ:", "  ահա   խնդրեմ ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_withOneTypo() {
        // "բարեւ ձեզ" vs "բարեվ ձեզ": 1 edit out of 9 characters -> similarity 0.89
        assertThat(evaluate("Բարեւ ձեզ", "Բարեվ ձեզ")).isEqualTo(new Evaluation(4, false));
    }

    @Test
    void evaluate_withIncompleteAnswer() {
        // "ահա խնդրեմ" vs "ահա": 7 edits out of 10 characters -> similarity 0.3
        assertThat(evaluate("Ահա, խնդրեմ:", "Ահա")).isEqualTo(new Evaluation(1, false));
    }

    @Test
    void evaluate_withUnrelatedAnswer() {
        assertThat(evaluate("Բարեւ ձեզ", "Ահա, խնդրեմ:").rawQuality()).isLessThanOrEqualTo(1);
    }

    @Test
    void evaluate_withEmptyAnswer() {
        assertThat(evaluate("Բարեւ ձեզ", "   ")).isEqualTo(new Evaluation(0, false));
    }
}
```

- [ ] **Step 4: Run it** — `mvn test -Dtest=TypingAnswerEvaluatorTest` → compilation failure.

- [ ] **Step 5: Implement `TypingAnswerEvaluator`**

```java
package com.blbulyandavbulyan.larm.exercise.evaluation;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.exercise.Answer;
import com.blbulyandavbulyan.larm.exercise.TextAnswer;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import org.springframework.stereotype.Component;

/**
 * Compares typed text with the expected phrase using normalized Levenshtein similarity.
 */
@Component
public class TypingAnswerEvaluator implements AnswerEvaluator {

    private static final int MAX_QUALITY = 5;

    private static final Pattern PUNCTUATION = Pattern.compile("\\p{P}+");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    @Override
    public ExerciseType supportedType() {
        return ExerciseType.TYPING;
    }

    @Override
    public Evaluation evaluate(ExpectedAnswer expected, Answer answer) {
        if (!(answer instanceof TextAnswer(String typedText))) {
            throw new UnsupportedAnswerTypeException(answer.type(), supportedType());
        }
        String expectedText = normalize(expected.text());
        String actualText = normalize(typedText);
        if (expectedText.equals(actualText)) {
            return new Evaluation(MAX_QUALITY, true);
        }
        if (actualText.isEmpty()) {
            return new Evaluation(0, false);
        }
        double similarity = 1.0 - (double) levenshtein(expectedText, actualText) / Math.max(expectedText.length(), actualText.length());
        return new Evaluation(qualityFor(similarity), false);
    }

    /**
     * Normalizes text: NFKC (maps the ligature "և" to "եւ"), lower case, no punctuation, single spaces.
     */
    static String normalize(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        normalized = PUNCTUATION.matcher(normalized).replaceAll("");
        return WHITESPACE.matcher(normalized).replaceAll(" ").trim();
    }

    private static int qualityFor(double similarity) {
        if (similarity > 0.8) {
            return 4;
        }
        if (similarity > 0.6) {
            return 3;
        }
        if (similarity > 0.4) {
            return 2;
        }
        if (similarity > 0.2) {
            return 1;
        }
        return 0;
    }

    private static int levenshtein(String first, String second) {
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];
        for (int j = 0; j <= second.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= second.length(); j++) {
                int substitution = previous[j - 1] + (first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(substitution, Math.min(previous[j], current[j - 1]) + 1);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[second.length()];
    }
}
```

Punctuation is **removed**, not replaced by a space, because `՞` sits inside words (`Ինչպե՞ս`).

- [ ] **Step 6: Map `UnsupportedAnswerTypeException` to 400** in `GlobalControllerAdvice` (add the import):

```java
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleUnsupportedAnswerType(UnsupportedAnswerTypeException e) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage())).build();
    }
```

- [ ] **Step 7: Run** — `mvn test -Dtest=TypingAnswerEvaluatorTest` → all pass; `mvn -q compile` → `BUILD SUCCESS`.

- [ ] **Step 8: Commit** — `git commit -m "feat(exercise): add answer model and typing evaluator"`

---

## Task 7: Content Item Resolver

**Files:** create `learning/ContentItemResolver.java`, `learning/ContentItemResolvers.java`,
`dialogue/DialogueContentItemResolver.java`.

- [ ] **Step 1: The contract** (spec §4)

```java
package com.blbulyandavbulyan.larm.learning;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import org.jspecify.annotations.Nullable;

/**
 * Gives the exercise engine access to one content type without it knowing that type's tables.
 */
public interface ContentItemResolver {

    ContentItemType supportedType();

    /**
     * Exercise types offered for this content type; enrolment creates one card per type and variant.
     */
    Set<ExerciseType> supportedExerciseTypes();

    boolean exists(UUID contentItemId);

    /**
     * Variants that are practised separately (speakers of a dialogue); empty means a single card without variant.
     */
    List<UUID> resolveVariantIds(UUID contentItemId);

    /**
     * Ordered sub-items to answer for the variant, or all sub-items when the variant is null.
     */
    List<UUID> resolveSubItemIds(UUID contentItemId, @Nullable UUID variantId);

    /**
     * Expected answer of a sub-item, empty when the sub-item does not belong to the content item.
     */
    Optional<ExpectedAnswer> resolveExpectedAnswer(UUID contentItemId, UUID contentSubItemId);
}
```

```java
package com.blbulyandavbulyan.larm.learning;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link ContentItemResolver} bean for a content type.
 */
@Component
public class ContentItemResolvers {

    private final Map<ContentItemType, ContentItemResolver> resolversByType;

    public ContentItemResolvers(List<ContentItemResolver> resolvers) {
        this.resolversByType = resolvers.stream()
                .collect(Collectors.toMap(ContentItemResolver::supportedType, Function.identity()));
    }

    public ContentItemResolver forType(ContentItemType contentItemType) {
        ContentItemResolver resolver = resolversByType.get(contentItemType);
        if (resolver == null) {
            throw new IllegalStateException("No content item resolver for type " + contentItemType);
        }
        return resolver;
    }
}
```

- [ ] **Step 2: The dialogue implementation**

```java
package com.blbulyandavbulyan.larm.dialogue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.repository.DialoguePhraseRepository;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
import com.blbulyandavbulyan.larm.dao.repository.DialogueSpeakerRepository;
import com.blbulyandavbulyan.larm.learning.ContentItemResolver;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Dialogues for exercises: variants are speakers, sub-items are lines (dialogue phrases).
 */
@Component
@RequiredArgsConstructor
public class DialogueContentItemResolver implements ContentItemResolver {

    private final DialogueRepository dialogueRepository;

    private final DialogueSpeakerRepository dialogueSpeakerRepository;

    private final DialoguePhraseRepository dialoguePhraseRepository;

    @Override
    public ContentItemType supportedType() {
        return ContentItemType.DIALOGUE;
    }

    @Override
    public Set<ExerciseType> supportedExerciseTypes() {
        return EnumSet.of(ExerciseType.TYPING);
    }

    @Override
    public boolean exists(UUID contentItemId) {
        return dialogueRepository.existsById(contentItemId);
    }

    @Override
    public List<UUID> resolveVariantIds(UUID contentItemId) {
        return dialogueSpeakerRepository.findIdsByDialogueId(contentItemId);
    }

    @Override
    public List<UUID> resolveSubItemIds(UUID contentItemId, @Nullable UUID variantId) {
        if (variantId == null) {
            return dialoguePhraseRepository.findIdsByDialogueId(contentItemId);
        }
        return dialoguePhraseRepository.findIdsByDialogueIdAndSpeakerId(contentItemId, variantId);
    }

    @Override
    public Optional<ExpectedAnswer> resolveExpectedAnswer(UUID contentItemId, UUID contentSubItemId) {
        return dialoguePhraseRepository.findPhraseText(contentItemId, contentSubItemId)
                .map(view -> new ExpectedAnswer(view.getText(), view.getTranscription()));
    }
}
```

- [ ] **Step 3: Compile** — `mvn -q compile` → `BUILD SUCCESS`. (It is covered end-to-end by the controller ITs.)

- [ ] **Step 4: Commit** — `git commit -m "feat(learning): add content item resolver with dialogue implementation"`

---

## Task 8: Learning Item Service

**Files:** create `learning/LearningItemResource.java`, `learning/LearningCardResource.java`,
`learning/EnrolmentResult.java`, `learning/LearningItemService.java`.

- [ ] **Step 1: Records**

```java
package com.blbulyandavbulyan.larm.learning;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import org.jspecify.annotations.Nullable;

public record LearningItemResource(UUID id, UUID contentItemId, ContentItemType contentItemType, Instant enrolledAt,
                                   @Nullable Instant excludedAt) {

    static LearningItemResource from(LearningItem item) {
        return new LearningItemResource(item.getId(), item.getContentItemId(), item.getContentItemType(),
                item.getEnrolledAt(), item.getExcludedAt());
    }
}
```

```java
package com.blbulyandavbulyan.larm.learning;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import org.jspecify.annotations.Nullable;

public record LearningCardResource(UUID id, UUID learningItemId, ExerciseType exerciseType, @Nullable UUID variantId,
                                   Instant nextReviewAt, int intervalDays, int repetitionCount, double easeFactor) {

    static LearningCardResource from(LearningCard card) {
        return new LearningCardResource(card.getId(), card.getLearningItem().getId(), card.getExerciseType(), card.getVariantId(),
                card.getNextReviewAt(), card.getIntervalDays(), card.getRepetitionCount(), card.getEaseFactor());
    }
}
```

```java
package com.blbulyandavbulyan.larm.learning;

/**
 * Enrolment outcome; {@code created} is false when the item already existed (re-enrolment).
 */
public record EnrolmentResult(LearningItemResource item, boolean created) {
}
```

- [ ] **Step 2: Service**

The initial ease factor is the SM-2 default (2.5). It is duplicated here on purpose so that
`learning` does not depend on `exercise`.

```java
package com.blbulyandavbulyan.larm.learning;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.exception.EntityNotFoundException;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningItemRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningItemService {

    private static final double INITIAL_EASE_FACTOR = 2.5;

    private final LearningItemRepository learningItemRepository;

    private final LearningCardRepository learningCardRepository;

    private final ContentItemResolvers contentItemResolvers;

    private final Clock clock;

    /**
     * Enrols the content item, or re-enrols it, and makes sure every card it should have exists.
     */
    @Transactional
    public EnrolmentResult enrol(UUID userId, UUID contentItemId, ContentItemType contentItemType) {
        ContentItemResolver resolver = contentItemResolvers.forType(contentItemType);
        if (!resolver.exists(contentItemId)) {
            throw new EntityNotFoundException("Content item %s of type %s not found".formatted(contentItemId, contentItemType));
        }
        Instant now = Instant.now(clock);
        Optional<LearningItem> existing = learningItemRepository
                .findByUserIdAndContentItemIdAndContentItemType(userId, contentItemId, contentItemType);
        LearningItem item = existing.orElseGet(() -> learningItemRepository.save(LearningItem.builder()
                .userId(userId)
                .contentItemId(contentItemId)
                .contentItemType(contentItemType)
                .enrolledAt(now)
                .build()));
        item.setExcludedAt(null);
        createMissingCards(item, resolver, now);
        return new EnrolmentResult(LearningItemResource.from(item), existing.isEmpty());
    }

    @Transactional
    public void exclude(UUID userId, UUID learningItemId) {
        LearningItem item = findOwned(userId, learningItemId);
        if (!item.isExcluded()) {
            item.setExcludedAt(Instant.now(clock));
        }
    }

    @Transactional(readOnly = true)
    public LearningItemResource findActiveByContent(UUID userId, UUID contentItemId, ContentItemType contentItemType) {
        return learningItemRepository.findByUserIdAndContentItemIdAndContentItemType(userId, contentItemId, contentItemType)
                .filter(item -> !item.isExcluded())
                .map(LearningItemResource::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active learning item for content item %s of type %s".formatted(contentItemId, contentItemType)));
    }

    @Transactional(readOnly = true)
    public List<LearningCardResource> getCards(UUID userId, UUID learningItemId) {
        LearningItem item = findOwned(userId, learningItemId);
        return learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(item.getId()).stream()
                .map(LearningCardResource::from)
                .toList();
    }

    private LearningItem findOwned(UUID userId, UUID learningItemId) {
        return learningItemRepository.findByIdAndUserId(learningItemId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Learning item %s not found".formatted(learningItemId)));
    }

    private void createMissingCards(LearningItem item, ContentItemResolver resolver, Instant now) {
        Set<CardKey> existingKeys = learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(item.getId()).stream()
                .map(card -> new CardKey(card.getExerciseType(), card.getVariantId()))
                .collect(Collectors.toSet());
        List<UUID> variantIds = resolver.resolveVariantIds(item.getContentItemId());
        List<@Nullable UUID> variants = variantIds.isEmpty() ? Collections.singletonList(null) : variantIds;

        List<LearningCard> missingCards = new ArrayList<>();
        for (ExerciseType exerciseType : resolver.supportedExerciseTypes()) {
            for (UUID variantId : variants) {
                if (!existingKeys.contains(new CardKey(exerciseType, variantId))) {
                    missingCards.add(LearningCard.builder()
                            .learningItem(item)
                            .exerciseType(exerciseType)
                            .variantId(variantId)
                            .nextReviewAt(now)
                            .intervalDays(0)
                            .easeFactor(INITIAL_EASE_FACTOR)
                            .repetitionCount(0)
                            .createdAt(now)
                            .updatedAt(now)
                            .build());
                }
            }
        }
        learningCardRepository.saveAll(missingCards);
    }

    private record CardKey(ExerciseType exerciseType, @Nullable UUID variantId) {
    }
}
```

- [ ] **Step 3: Compile** — `mvn -q compile` → `BUILD SUCCESS`.

- [ ] **Step 4: Commit** — `git commit -m "feat(learning): add learning item service"`

---

## Task 9: Exercise Services

**Files:** create the records `exercise/AttemptCommand.java`, `AttemptResultResource.java`,
`AttemptResource.java`, `SubItemAttemptsResource.java`, `SessionResource.java`,
`SessionStartResult.java`, `SessionStatusResource.java`, `CompletionResource.java`,
`DueCardResource.java`, `HistoryItemResource.java`, plus `ExerciseSessionService.java`,
`ExerciseQueueService.java`, `ExerciseHistoryService.java`.

- [ ] **Step 1: Records** (one file each, package `com.blbulyandavbulyan.larm.exercise`)

```java
public record AttemptCommand(UUID contentSubItemId, boolean usedHint, Answer answer) {
}
```

```java
/**
 * Immediate feedback for one attempt; {@code expectedText} is only set when the answer was not correct.
 */
public record AttemptResultResource(UUID attemptId, UUID contentSubItemId, boolean usedHint, int quality, boolean correct,
                                    @Nullable String expectedText) {
}
```

```java
public record AttemptResource(UUID attemptId, AnswerType answerType, @Nullable String answerText, boolean usedHint,
                              int quality, boolean correct, Instant evaluatedAt) {

    static AttemptResource from(ExerciseAttempt attempt) {
        return new AttemptResource(attempt.getId(), attempt.getAnswerType(), attempt.getAnswerText(), attempt.isUsedHint(),
                attempt.getQuality(), attempt.isCorrect(), attempt.getEvaluatedAt());
    }
}
```

```java
public record SubItemAttemptsResource(UUID contentSubItemId, List<AttemptResource> attempts) {
}
```

```java
public record SessionResource(UUID sessionId, UUID learningCardId, UUID learningItemId, UUID contentItemId,
                              ContentItemType contentItemType, ExerciseType exerciseType, @Nullable UUID variantId,
                              List<UUID> subItemIds, Instant startedAt, @Nullable Instant completedAt) {
}
```

```java
/**
 * Started or resumed session; {@code created} is false when an open session was returned.
 */
public record SessionStartResult(SessionResource session, boolean created) {
}
```

```java
public record SessionStatusResource(SessionResource session, List<SubItemAttemptsResource> attemptedSubItems,
                                    List<UUID> pendingSubItemIds) {
}
```

```java
public record CompletionResource(UUID sessionId, Instant completedAt, int aggregateScore, int previousIntervalDays,
                                 int nextIntervalDays, Instant nextReviewAt, double easeFactor) {
}
```

```java
public record DueCardResource(UUID learningCardId, UUID learningItemId, UUID contentItemId, ContentItemType contentItemType,
                              ExerciseType exerciseType, @Nullable UUID variantId, Instant nextReviewAt, int intervalDays,
                              int repetitionCount) {

    static DueCardResource from(LearningCard card) {
        LearningItem item = card.getLearningItem();
        return new DueCardResource(card.getId(), item.getId(), item.getContentItemId(), item.getContentItemType(),
                card.getExerciseType(), card.getVariantId(), card.getNextReviewAt(), card.getIntervalDays(), card.getRepetitionCount());
    }
}
```

```java
public record HistoryItemResource(UUID sessionId, UUID learningCardId, UUID learningItemId, UUID contentItemId,
                                  ContentItemType contentItemType, ExerciseType exerciseType, @Nullable UUID variantId,
                                  Instant startedAt, Instant completedAt, int aggregateScore) {

    static HistoryItemResource from(ExerciseSession session) {
        LearningCard card = session.getLearningCard();
        LearningItem item = card.getLearningItem();
        return new HistoryItemResource(session.getId(), card.getId(), item.getId(), item.getContentItemId(),
                item.getContentItemType(), card.getExerciseType(), card.getVariantId(), session.getStartedAt(),
                session.getCompletedAt(), session.getAggregateScore());
    }
}
```

Imports per file: `java.time.Instant`, `java.util.List`, `java.util.UUID` as used; the `dao.entities`
types as used; `org.jspecify.annotations.Nullable` where `@Nullable` appears.

- [ ] **Step 2: `ExerciseSessionService.java`**

```java
package com.blbulyandavbulyan.larm.exercise;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.exception.ConflictException;
import com.blbulyandavbulyan.larm.core.exception.EntityNotFoundException;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseSession;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseAttemptRepository;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseSessionRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.exercise.evaluation.AnswerEvaluators;
import com.blbulyandavbulyan.larm.exercise.evaluation.Evaluation;
import com.blbulyandavbulyan.larm.exercise.srs.Sm2Algorithm;
import com.blbulyandavbulyan.larm.exercise.srs.Sm2Input;
import com.blbulyandavbulyan.larm.exercise.srs.Sm2Result;
import com.blbulyandavbulyan.larm.learning.ContentItemResolver;
import com.blbulyandavbulyan.larm.learning.ContentItemResolvers;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session lifecycle: start, attempts, recovery, completion (spec section 5).
 */
@Service
@RequiredArgsConstructor
public class ExerciseSessionService {

    static final int HINT_PENALTY = 1;

    private final LearningCardRepository learningCardRepository;

    private final ExerciseSessionRepository exerciseSessionRepository;

    private final ExerciseAttemptRepository exerciseAttemptRepository;

    private final ContentItemResolvers contentItemResolvers;

    private final AnswerEvaluators answerEvaluators;

    private final Sm2Algorithm sm2Algorithm;

    private final Clock clock;

    /**
     * Starts a session for the card, or returns the open one; the card row lock serializes parallel starts.
     */
    @Transactional
    public SessionStartResult startSession(UUID userId, UUID learningCardId) {
        LearningCard card = learningCardRepository.findByIdAndUserIdForUpdate(learningCardId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Learning card %s not found".formatted(learningCardId)));
        if (card.getLearningItem().isExcluded()) {
            throw new ConflictException("Learning item %s is excluded".formatted(card.getLearningItem().getId()));
        }
        Optional<ExerciseSession> openSession = exerciseSessionRepository.findByLearningCardIdAndCompletedAtIsNull(card.getId());
        ExerciseSession session = openSession.orElseGet(() -> exerciseSessionRepository.save(ExerciseSession.builder()
                .learningCard(card)
                .userId(userId)
                .startedAt(Instant.now(clock))
                .build()));
        return new SessionStartResult(toResource(session, card), openSession.isEmpty());
    }

    @Transactional(readOnly = true)
    public SessionStatusResource getSession(UUID userId, UUID sessionId) {
        ExerciseSession session = exerciseSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> sessionNotFound(sessionId));
        SessionResource resource = toResource(session, session.getLearningCard());
        Map<UUID, List<AttemptResource>> attemptsBySubItem = exerciseAttemptRepository
                .findBySessionIdOrderByEvaluatedAtAscIdAsc(sessionId).stream()
                .collect(Collectors.groupingBy(ExerciseAttempt::getContentSubItemId, LinkedHashMap::new,
                        Collectors.mapping(AttemptResource::from, Collectors.toList())));
        List<SubItemAttemptsResource> attempted = resource.subItemIds().stream()
                .filter(attemptsBySubItem::containsKey)
                .map(subItemId -> new SubItemAttemptsResource(subItemId, attemptsBySubItem.get(subItemId)))
                .toList();
        List<UUID> pending = resource.subItemIds().stream()
                .filter(subItemId -> !attemptsBySubItem.containsKey(subItemId))
                .toList();
        return new SessionStatusResource(resource, attempted, pending);
    }

    @Transactional
    public AttemptResultResource submitAttempt(UUID userId, UUID sessionId, AttemptCommand command) {
        ExerciseSession session = lockOpenSession(userId, sessionId);
        LearningCard card = session.getLearningCard();
        LearningItem item = card.getLearningItem();
        ContentItemResolver resolver = contentItemResolvers.forType(item.getContentItemType());
        if (!resolver.resolveSubItemIds(item.getContentItemId(), card.getVariantId()).contains(command.contentSubItemId())) {
            throw subItemNotInSession(command.contentSubItemId(), sessionId);
        }
        ExpectedAnswer expected = resolver.resolveExpectedAnswer(item.getContentItemId(), command.contentSubItemId())
                .orElseThrow(() -> subItemNotInSession(command.contentSubItemId(), sessionId));

        Evaluation evaluation = answerEvaluators.forType(card.getExerciseType()).evaluate(expected, command.answer());
        int quality = command.usedHint() ? Math.max(0, evaluation.rawQuality() - HINT_PENALTY) : evaluation.rawQuality();
        ExerciseAttempt attempt = exerciseAttemptRepository.save(ExerciseAttempt.builder()
                .session(session)
                .contentSubItemId(command.contentSubItemId())
                .answerType(command.answer().type())
                .answerText(answerText(command.answer()))
                .usedHint(command.usedHint())
                .rawQuality(evaluation.rawQuality())
                .quality(quality)
                .correct(evaluation.correct())
                .evaluatedAt(Instant.now(clock))
                .build());
        return new AttemptResultResource(attempt.getId(), command.contentSubItemId(), command.usedHint(), quality,
                evaluation.correct(), evaluation.correct() ? null : expected.text());
    }

    /**
     * Seals the session and applies SM-2 to its card.
     */
    @Transactional
    public CompletionResource completeSession(UUID userId, UUID sessionId) {
        ExerciseSession session = lockOpenSession(userId, sessionId);
        LearningCard card = session.getLearningCard();
        List<UUID> subItemIds = resolveSubItemIds(card);
        if (subItemIds.isEmpty()) {
            throw new ConflictException("Session %s has nothing to answer".formatted(sessionId));
        }
        Map<UUID, List<Integer>> qualitiesBySubItem = exerciseAttemptRepository.findBySessionIdOrderByEvaluatedAtAscIdAsc(sessionId).stream()
                .collect(Collectors.groupingBy(ExerciseAttempt::getContentSubItemId,
                        Collectors.mapping(ExerciseAttempt::getQuality, Collectors.toList())));
        List<UUID> pending = subItemIds.stream().filter(subItemId -> !qualitiesBySubItem.containsKey(subItemId)).toList();
        if (!pending.isEmpty()) {
            throw new ConflictException("Session %s has sub-items without attempts: %s".formatted(sessionId, pending));
        }

        Sm2Result result = sm2Algorithm.apply(new Sm2Input(card.getRepetitionCount(), card.getIntervalDays(), card.getEaseFactor(),
                subItemIds.stream().map(qualitiesBySubItem::get).toList()));
        Instant now = Instant.now(clock);
        int previousIntervalDays = card.getIntervalDays();
        card.setRepetitionCount(result.repetitionCount());
        card.setIntervalDays(result.intervalDays());
        card.setEaseFactor(result.easeFactor());
        card.setNextReviewAt(now.plus(result.intervalDays(), ChronoUnit.DAYS));
        card.setUpdatedAt(now);
        session.setCompletedAt(now);
        session.setAggregateScore(result.aggregateScore());
        return new CompletionResource(session.getId(), now, result.aggregateScore(), previousIntervalDays,
                result.intervalDays(), card.getNextReviewAt(), result.easeFactor());
    }

    private ExerciseSession lockOpenSession(UUID userId, UUID sessionId) {
        ExerciseSession session = exerciseSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> sessionNotFound(sessionId));
        if (session.isCompleted()) {
            throw new ConflictException("Session %s is already completed".formatted(sessionId));
        }
        return session;
    }

    private List<UUID> resolveSubItemIds(LearningCard card) {
        LearningItem item = card.getLearningItem();
        return contentItemResolvers.forType(item.getContentItemType()).resolveSubItemIds(item.getContentItemId(), card.getVariantId());
    }

    private SessionResource toResource(ExerciseSession session, LearningCard card) {
        LearningItem item = card.getLearningItem();
        return new SessionResource(session.getId(), card.getId(), item.getId(), item.getContentItemId(), item.getContentItemType(),
                card.getExerciseType(), card.getVariantId(), resolveSubItemIds(card), session.getStartedAt(), session.getCompletedAt());
    }

    private static @Nullable String answerText(Answer answer) {
        return switch (answer) {
            case TextAnswer textAnswer -> textAnswer.text();
        };
    }

    private static EntityNotFoundException sessionNotFound(UUID sessionId) {
        return new EntityNotFoundException("Exercise session %s not found".formatted(sessionId));
    }

    private static ConflictException subItemNotInSession(UUID subItemId, UUID sessionId) {
        return new ConflictException("Sub-item %s is not part of session %s".formatted(subItemId, sessionId));
    }
}
```

- [ ] **Step 3: `ExerciseQueueService.java` and `ExerciseHistoryService.java`**

```java
package com.blbulyandavbulyan.larm.exercise;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseQueueService {

    private final LearningCardRepository learningCardRepository;

    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<DueCardResource> getDueCards(UUID userId, @Nullable ExerciseType exerciseType,
                                             @Nullable ContentItemType contentItemType, Pageable pageable) {
        return learningCardRepository.findDueCards(userId, Instant.now(clock), exerciseType, contentItemType, pageable)
                .map(DueCardResource::from);
    }
}
```

```java
package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseSessionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseHistoryService {

    private final ExerciseSessionRepository exerciseSessionRepository;

    @Transactional(readOnly = true)
    public Page<HistoryItemResource> getHistory(UUID userId, @Nullable Instant from, @Nullable Instant to,
                                                @Nullable ExerciseType exerciseType, @Nullable ContentItemType contentItemType,
                                                Pageable pageable) {
        return exerciseSessionRepository.findCompleted(userId, from, to, exerciseType, contentItemType, pageable)
                .map(HistoryItemResource::from);
    }
}
```

`Page.map` builds the new content list immediately, so mapping inside the transaction is safe.

- [ ] **Step 4: Compile** — `mvn -q compile` → `BUILD SUCCESS`.

- [ ] **Step 5: Commit** — `git commit -m "feat(exercise): add session, queue and history services"`

---

## Task 10: OpenAPI Descriptions

**Files:** modify `api/openapi/OpenApiConstants.java`.

- [ ] **Step 1: Add to `Descriptions`**

```java
        String CONTENT_ITEM_ID = "Id of the learnable content item, e.g. the dialogue id";
        String CONTENT_ITEM_TYPE = "Type of the content item";
        String LEARNING_ITEM_ID = "Id of the user's enrolment of a content item";
        String LEARNING_CARD_ID = "Id of the spaced repetition card (one per exercise type and variant)";
        String ENROLLED_AT = "When the user first enrolled the content item";
        String EXCLUDED_AT = "When the user excluded the item from studying; null while active";
        String EXERCISE_TYPE = "Type of exercise, defines how answers are given and evaluated";
        String VARIANT_ID = "Part of the content practised separately; for dialogues the id of the speaker the user plays";
        String NEXT_REVIEW_AT = "When the card is due for its next review";
        String INTERVAL_DAYS = "Current review interval in days";
        String REPETITION_COUNT = "Number of successful reviews in a row";
        String EASE_FACTOR = "SM-2 ease factor (minimum 1.3); higher means intervals grow faster";
        String SESSION_ID = "Id of the exercise session";
        String SUB_ITEM_ID = "Id of the sub-item being answered; for dialogues the dialogue phrase id";
        String SUB_ITEM_IDS = "Sub-items the user has to answer in this session, in order";
        String PENDING_SUB_ITEM_IDS = "Sub-items of the session without any attempt yet";
        String STARTED_AT = "When the session was started";
        String COMPLETED_AT = "When the session was completed; null while in progress";
        String USED_HINT = "Whether the user looked at a hint; lowers the quality by one";
        String ANSWER = "The answer; the type field selects the kind of answer";
        String ANSWER_TYPE = "Kind of answer";
        String ANSWER_TEXT = "The typed text";
        String ATTEMPT_ID = "Id of the attempt";
        String QUALITY = "SM-2 quality 0-5 after the hint penalty";
        String CORRECT = "Whether the answer matched the expected answer exactly (independent of hints)";
        String EXPECTED_TEXT = "Correct answer, only present when the answer was not correct";
        String EVALUATED_AT = "When the attempt was evaluated";
        String AGGREGATE_SCORE = "Session score 0-100";
        String PREVIOUS_INTERVAL_DAYS = "Review interval in days before this session";
        String NEXT_INTERVAL_DAYS = "Review interval in days after this session";
        String PAGE = "Zero-based page index";
        String PAGE_SIZE = "Page size";
        String TOTAL_ELEMENTS = "Total number of matching elements";
```

- [ ] **Step 2: Compile and commit** — `mvn -q compile`; `git commit -am "docs(openapi): add spaced repetition descriptions"`

---

## Task 11: Learning Items API

**Files:** create in `api/learning/`: `EnrolLearningItemRequest.java`, `LearningItemResponse.java`,
`LearningCardsResponse.java`, `LearningResponseMapper.java`, `LearningItemsApi.java`,
`LearningItemsController.java`.

- [ ] **Step 1: DTOs**

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Enrol Learning Item Request")
public record EnrolLearningItemRequest(
        @Schema(description = Descriptions.CONTENT_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID contentItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ContentItemType contentItemType) {
}
```

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Learning Item Response")
@Builder
public record LearningItemResponse(
        @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = Descriptions.CONTENT_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID contentItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        ContentItemType contentItemType,

        @Schema(description = Descriptions.ENROLLED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant enrolledAt,

        @Schema(description = Descriptions.EXCLUDED_AT)
        @Nullable
        Instant excludedAt) {
}
```

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Learning Cards Response")
public record LearningCardsResponse(List<LearningCardResponse> cards) {

    @Schema(name = "Learning Card Response")
    @Builder
    public record LearningCardResponse(
            @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID learningCardId,

            @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID learningItemId,

            @Schema(description = Descriptions.EXERCISE_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            ExerciseType exerciseType,

            @Schema(description = Descriptions.VARIANT_ID)
            @Nullable
            UUID variantId,

            @Schema(description = Descriptions.NEXT_REVIEW_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant nextReviewAt,

            @Schema(description = Descriptions.INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int intervalDays,

            @Schema(description = Descriptions.REPETITION_COUNT, requiredMode = Schema.RequiredMode.REQUIRED)
            int repetitionCount,

            @Schema(description = Descriptions.EASE_FACTOR, requiredMode = Schema.RequiredMode.REQUIRED)
            double easeFactor) {
    }
}
```

- [ ] **Step 2: Mapper**

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.util.List;

import com.blbulyandavbulyan.larm.learning.LearningCardResource;
import com.blbulyandavbulyan.larm.learning.LearningItemResource;
import org.springframework.stereotype.Component;

@Component
class LearningResponseMapper {

    LearningItemResponse toResponse(LearningItemResource item) {
        return LearningItemResponse.builder()
                .id(item.id())
                .contentItemId(item.contentItemId())
                .contentItemType(item.contentItemType())
                .enrolledAt(item.enrolledAt())
                .excludedAt(item.excludedAt())
                .build();
    }

    LearningCardsResponse toResponse(List<LearningCardResource> cards) {
        return new LearningCardsResponse(cards.stream()
                .map(card -> LearningCardsResponse.LearningCardResponse.builder()
                        .learningCardId(card.id())
                        .learningItemId(card.learningItemId())
                        .exerciseType(card.exerciseType())
                        .variantId(card.variantId())
                        .nextReviewAt(card.nextReviewAt())
                        .intervalDays(card.intervalDays())
                        .repetitionCount(card.repetitionCount())
                        .easeFactor(card.easeFactor())
                        .build())
                .toList());
    }
}
```

- [ ] **Step 3: `LearningItemsApi.java`**

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.advice.ValidationErrorResponse;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/learning-items")
@Tag(name = "Learning items", description = "Enrol content (dialogues) for spaced repetition study")
interface LearningItemsApi {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enrol a content item",
            description = """
                    Enrols the content item and creates one card per exercise type and speaker, due immediately. \
                    Enrolling again re-activates an excluded item and creates missing cards; progress is kept.\
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "Enrolled",
                            content = @Content(schema = @Schema(implementation = LearningItemResponse.class))),
                    @ApiResponse(responseCode = "200", description = "Already enrolled, re-activated",
                            content = @Content(schema = @Schema(implementation = LearningItemResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation failed",
                            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Content item not found")
            })
    ResponseEntity<LearningItemResponse> enrol(DatabaseUserJwtAuthenticationToken auth,
                                               @Valid @RequestBody EnrolLearningItemRequest request);

    @DeleteMapping("/{learningItemId}")
    @Operation(summary = "Exclude a learning item from studying; progress is kept", responses = {
            @ApiResponse(responseCode = "204", description = "Excluded"),
            @ApiResponse(responseCode = "404", description = "Learning item not found")
    })
    ResponseEntity<Void> exclude(DatabaseUserJwtAuthenticationToken auth, @PathVariable("learningItemId") UUID learningItemId);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find the active learning item of a content item", responses = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = LearningItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not enrolled or excluded")
    })
    LearningItemResponse findByContent(DatabaseUserJwtAuthenticationToken auth,
                                       @RequestParam("contentItemId") UUID contentItemId,
                                       @RequestParam("contentItemType") ContentItemType contentItemType);

    @GetMapping(value = "/{learningItemId}/cards", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the spaced repetition cards of a learning item", responses = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = LearningCardsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Learning item not found")
    })
    LearningCardsResponse getCards(DatabaseUserJwtAuthenticationToken auth, @PathVariable("learningItemId") UUID learningItemId);
}
```

- [ ] **Step 4: `LearningItemsController.java`**

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.learning.EnrolmentResult;
import com.blbulyandavbulyan.larm.learning.LearningItemService;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class LearningItemsController implements LearningItemsApi {

    private final LearningItemService learningItemService;

    private final LearningResponseMapper mapper;

    @Override
    public ResponseEntity<LearningItemResponse> enrol(DatabaseUserJwtAuthenticationToken auth, EnrolLearningItemRequest request) {
        EnrolmentResult result = learningItemService.enrol(auth.getUserId(), request.contentItemId(), request.contentItemType());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(mapper.toResponse(result.item()));
    }

    @Override
    public ResponseEntity<Void> exclude(DatabaseUserJwtAuthenticationToken auth, UUID learningItemId) {
        learningItemService.exclude(auth.getUserId(), learningItemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public LearningItemResponse findByContent(DatabaseUserJwtAuthenticationToken auth, UUID contentItemId,
                                              ContentItemType contentItemType) {
        return mapper.toResponse(learningItemService.findActiveByContent(auth.getUserId(), contentItemId, contentItemType));
    }

    @Override
    public LearningCardsResponse getCards(DatabaseUserJwtAuthenticationToken auth, UUID learningItemId) {
        return mapper.toResponse(learningItemService.getCards(auth.getUserId(), learningItemId));
    }
}
```

- [ ] **Step 5: Compile and commit** — `mvn -q compile`; `git commit -m "feat(api): add learning items endpoints"`

---

## Task 12: Exercises API

**Files:** create in `api/exercises/`: `StartSessionRequest.java`, `AnswerRequest.java`,
`TextAnswerRequest.java`, `AttemptRequest.java`, `SessionResponse.java`, `AttemptResponse.java`,
`SessionStatusResponse.java`, `CompleteSessionResponse.java`, `QueueResponse.java`,
`HistoryResponse.java`, `ExerciseResponseMapper.java`, `ExercisesApi.java`, `ExercisesController.java`.

- [ ] **Step 1: Requests.** The answer is polymorphic on `type`; adding `AUDIO` later means adding one
subtype to `permits`, `@JsonSubTypes` and the schema mapping.

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Start Session Request")
public record StartSessionRequest(
        @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID learningCardId) {
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = TextAnswerRequest.class, name = "TEXT"))
@Schema(name = "Answer Request", description = Descriptions.ANSWER,
        oneOf = TextAnswerRequest.class,
        discriminatorProperty = "type",
        discriminatorMapping = @DiscriminatorMapping(value = "TEXT", schema = TextAnswerRequest.class))
public sealed interface AnswerRequest permits TextAnswerRequest {
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Text Answer Request")
public record TextAnswerRequest(
        @Schema(description = Descriptions.ANSWER_TEXT, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        String text) implements AnswerRequest {
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Attempt Request")
public record AttemptRequest(
        @Schema(description = Descriptions.SUB_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID contentSubItemId,

        @Schema(description = Descriptions.USED_HINT)
        boolean usedHint,

        @Schema(description = Descriptions.ANSWER, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        AnswerRequest answer) {
}
```

Jackson 3 keeps its annotations in `com.fasterxml.jackson.annotation`, so these imports are
correct even though databind is `tools.jackson`. A missing or unknown `type` fails
deserialization, and Spring answers that with 400.

- [ ] **Step 2: Responses**

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Session Response")
@Builder
public record SessionResponse(
        @Schema(description = Descriptions.SESSION_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID sessionId,

        @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID learningCardId,

        @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID learningItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID contentItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        ContentItemType contentItemType,

        @Schema(description = Descriptions.EXERCISE_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        ExerciseType exerciseType,

        @Schema(description = Descriptions.VARIANT_ID)
        @Nullable
        UUID variantId,

        @Schema(description = Descriptions.SUB_ITEM_IDS, requiredMode = Schema.RequiredMode.REQUIRED)
        List<UUID> subItemIds,

        @Schema(description = Descriptions.STARTED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant startedAt,

        @Schema(description = Descriptions.COMPLETED_AT)
        @Nullable
        Instant completedAt) {
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Attempt Response")
@Builder
public record AttemptResponse(
        @Schema(description = Descriptions.ATTEMPT_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID attemptId,

        @Schema(description = Descriptions.SUB_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID contentSubItemId,

        @Schema(description = Descriptions.USED_HINT, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean usedHint,

        @Schema(description = Descriptions.QUALITY, requiredMode = Schema.RequiredMode.REQUIRED)
        int quality,

        @Schema(description = Descriptions.CORRECT, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean correct,

        @Schema(description = Descriptions.EXPECTED_TEXT)
        @Nullable
        String expectedText) {
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Session Status Response")
public record SessionStatusResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        SessionResponse session,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<AttemptedSubItemResponse> attemptedSubItems,

        @Schema(description = Descriptions.PENDING_SUB_ITEM_IDS, requiredMode = Schema.RequiredMode.REQUIRED)
        List<UUID> pendingSubItemIds) {

    @Schema(name = "Attempted Sub Item Response")
    public record AttemptedSubItemResponse(
            @Schema(description = Descriptions.SUB_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID contentSubItemId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            List<AttemptHistoryResponse> attempts) {
    }

    @Schema(name = "Attempt History Response")
    @Builder
    public record AttemptHistoryResponse(
            @Schema(description = Descriptions.ATTEMPT_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID attemptId,

            @Schema(description = Descriptions.ANSWER_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            AnswerType answerType,

            @Schema(description = Descriptions.ANSWER_TEXT)
            @Nullable
            String answerText,

            @Schema(description = Descriptions.USED_HINT, requiredMode = Schema.RequiredMode.REQUIRED)
            boolean usedHint,

            @Schema(description = Descriptions.QUALITY, requiredMode = Schema.RequiredMode.REQUIRED)
            int quality,

            @Schema(description = Descriptions.CORRECT, requiredMode = Schema.RequiredMode.REQUIRED)
            boolean correct,

            @Schema(description = Descriptions.EVALUATED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant evaluatedAt) {
    }
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Complete Session Response")
public record CompleteSessionResponse(
        @Schema(description = Descriptions.SESSION_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID sessionId,

        @Schema(description = Descriptions.COMPLETED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant completedAt,

        @Schema(description = Descriptions.AGGREGATE_SCORE, requiredMode = Schema.RequiredMode.REQUIRED)
        int aggregateScore,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CardUpdateResponse cardUpdate) {

    @Schema(name = "Card Update Response")
    public record CardUpdateResponse(
            @Schema(description = Descriptions.PREVIOUS_INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int previousIntervalDays,

            @Schema(description = Descriptions.NEXT_INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int nextIntervalDays,

            @Schema(description = Descriptions.NEXT_REVIEW_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant nextReviewAt,

            @Schema(description = Descriptions.EASE_FACTOR, requiredMode = Schema.RequiredMode.REQUIRED)
            double easeFactor) {
    }
}
```

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Queue Response")
public record QueueResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<QueueItemResponse> items,

        @Schema(description = Descriptions.PAGE, requiredMode = Schema.RequiredMode.REQUIRED)
        int page,

        @Schema(description = Descriptions.PAGE_SIZE, requiredMode = Schema.RequiredMode.REQUIRED)
        int size,

        @Schema(description = Descriptions.TOTAL_ELEMENTS, requiredMode = Schema.RequiredMode.REQUIRED)
        long totalElements) {

    @Schema(name = "Queue Item Response")
    @Builder
    public record QueueItemResponse(
            @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID learningCardId,

            @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID learningItemId,

            @Schema(description = Descriptions.CONTENT_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID contentItemId,

            @Schema(description = Descriptions.CONTENT_ITEM_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            ContentItemType contentItemType,

            @Schema(description = Descriptions.EXERCISE_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            ExerciseType exerciseType,

            @Schema(description = Descriptions.VARIANT_ID)
            @Nullable
            UUID variantId,

            @Schema(description = Descriptions.NEXT_REVIEW_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant nextReviewAt,

            @Schema(description = Descriptions.INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int intervalDays,

            @Schema(description = Descriptions.REPETITION_COUNT, requiredMode = Schema.RequiredMode.REQUIRED)
            int repetitionCount) {
    }
}
```

`HistoryResponse` has the same shape as `QueueResponse`: `items`, `page`, `size`, `totalElements`.
Its nested `HistoryItemResponse` (`@Builder`) has the fields `sessionId`, `learningCardId`,
`learningItemId`, `contentItemId`, `contentItemType`, `exerciseType`, `@Nullable variantId`,
`startedAt`, `completedAt`, `aggregateScore`, using the descriptions of the same names (spec §6.4).

- [ ] **Step 3: Mapper**

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.util.List;
import java.util.function.Function;

import com.blbulyandavbulyan.larm.exercise.Answer;
import com.blbulyandavbulyan.larm.exercise.AttemptCommand;
import com.blbulyandavbulyan.larm.exercise.AttemptResultResource;
import com.blbulyandavbulyan.larm.exercise.CompletionResource;
import com.blbulyandavbulyan.larm.exercise.DueCardResource;
import com.blbulyandavbulyan.larm.exercise.HistoryItemResource;
import com.blbulyandavbulyan.larm.exercise.SessionResource;
import com.blbulyandavbulyan.larm.exercise.SessionStatusResource;
import com.blbulyandavbulyan.larm.exercise.TextAnswer;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
class ExerciseResponseMapper {

    AttemptCommand toCommand(AttemptRequest request) {
        return new AttemptCommand(request.contentSubItemId(), request.usedHint(), toAnswer(request.answer()));
    }

    SessionResponse toResponse(SessionResource session) {
        return SessionResponse.builder()
                .sessionId(session.sessionId())
                .learningCardId(session.learningCardId())
                .learningItemId(session.learningItemId())
                .contentItemId(session.contentItemId())
                .contentItemType(session.contentItemType())
                .exerciseType(session.exerciseType())
                .variantId(session.variantId())
                .subItemIds(session.subItemIds())
                .startedAt(session.startedAt())
                .completedAt(session.completedAt())
                .build();
    }

    SessionStatusResponse toResponse(SessionStatusResource status) {
        return new SessionStatusResponse(
                toResponse(status.session()),
                status.attemptedSubItems().stream()
                        .map(subItem -> new SessionStatusResponse.AttemptedSubItemResponse(subItem.contentSubItemId(),
                                subItem.attempts().stream()
                                        .map(attempt -> SessionStatusResponse.AttemptHistoryResponse.builder()
                                                .attemptId(attempt.attemptId())
                                                .answerType(attempt.answerType())
                                                .answerText(attempt.answerText())
                                                .usedHint(attempt.usedHint())
                                                .quality(attempt.quality())
                                                .correct(attempt.correct())
                                                .evaluatedAt(attempt.evaluatedAt())
                                                .build())
                                        .toList()))
                        .toList(),
                status.pendingSubItemIds());
    }

    AttemptResponse toResponse(AttemptResultResource result) {
        return AttemptResponse.builder()
                .attemptId(result.attemptId())
                .contentSubItemId(result.contentSubItemId())
                .usedHint(result.usedHint())
                .quality(result.quality())
                .correct(result.correct())
                .expectedText(result.expectedText())
                .build();
    }

    CompleteSessionResponse toResponse(CompletionResource completion) {
        return new CompleteSessionResponse(completion.sessionId(), completion.completedAt(), completion.aggregateScore(),
                new CompleteSessionResponse.CardUpdateResponse(completion.previousIntervalDays(), completion.nextIntervalDays(),
                        completion.nextReviewAt(), completion.easeFactor()));
    }

    QueueResponse toQueueResponse(Page<DueCardResource> page) {
        return new QueueResponse(mapContent(page, card -> QueueResponse.QueueItemResponse.builder()
                .learningCardId(card.learningCardId())
                .learningItemId(card.learningItemId())
                .contentItemId(card.contentItemId())
                .contentItemType(card.contentItemType())
                .exerciseType(card.exerciseType())
                .variantId(card.variantId())
                .nextReviewAt(card.nextReviewAt())
                .intervalDays(card.intervalDays())
                .repetitionCount(card.repetitionCount())
                .build()), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    HistoryResponse toHistoryResponse(Page<HistoryItemResource> page) {
        return new HistoryResponse(mapContent(page, item -> HistoryResponse.HistoryItemResponse.builder()
                .sessionId(item.sessionId())
                .learningCardId(item.learningCardId())
                .learningItemId(item.learningItemId())
                .contentItemId(item.contentItemId())
                .contentItemType(item.contentItemType())
                .exerciseType(item.exerciseType())
                .variantId(item.variantId())
                .startedAt(item.startedAt())
                .completedAt(item.completedAt())
                .aggregateScore(item.aggregateScore())
                .build()), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private static Answer toAnswer(AnswerRequest request) {
        return switch (request) {
            case TextAnswerRequest text -> new TextAnswer(text.text());
        };
    }

    private static <T, R> List<R> mapContent(Page<T> page, Function<T, R> mapper) {
        return page.getContent().stream().map(mapper).toList();
    }
}
```

- [ ] **Step 4: `ExercisesApi.java`**

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.advice.ValidationErrorResponse;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/exercises")
@Tag(name = "Exercises", description = "Spaced repetition queue, exercise sessions and history")
@ApiResponse(responseCode = "400", description = "Validation failed",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class)))
interface ExercisesApi {

    @GetMapping(value = "/queue", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cards due for review, oldest due first")
    QueueResponse getQueue(DatabaseUserJwtAuthenticationToken auth,
                           @RequestParam(name = "exerciseType", required = false) @Nullable ExerciseType exerciseType,
                           @RequestParam(name = "contentItemType", required = false) @Nullable ContentItemType contentItemType,
                           @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                           @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size);

    @PostMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start a session for a card, or resume its open session", responses = {
            @ApiResponse(responseCode = "201", description = "Session created",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "200", description = "Open session resumed",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "409", description = "Learning item is excluded")
    })
    ResponseEntity<SessionResponse> startSession(DatabaseUserJwtAuthenticationToken auth, @Valid @RequestBody StartSessionRequest request);

    @GetMapping(value = "/sessions/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Session state with all attempts, for resuming after a restart", responses = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    SessionStatusResponse getSession(DatabaseUserJwtAuthenticationToken auth, @PathVariable("sessionId") UUID sessionId);

    @PostMapping(value = "/sessions/{sessionId}/attempts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Evaluate one answer; retries of the same sub-item are allowed", responses = {
            @ApiResponse(responseCode = "200", description = "Evaluated"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Session completed, or sub-item not part of the session")
    })
    AttemptResponse submitAttempt(DatabaseUserJwtAuthenticationToken auth, @PathVariable("sessionId") UUID sessionId,
                                  @Valid @RequestBody AttemptRequest request);

    @PostMapping(value = "/sessions/{sessionId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Complete the session and schedule the next review", responses = {
            @ApiResponse(responseCode = "200", description = "Completed"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Sub-items without attempts, or already completed")
    })
    CompleteSessionResponse completeSession(DatabaseUserJwtAuthenticationToken auth, @PathVariable("sessionId") UUID sessionId);

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Completed sessions, newest first")
    HistoryResponse getHistory(DatabaseUserJwtAuthenticationToken auth,
                               @RequestParam(name = "from", required = false) @Nullable Instant from,
                               @RequestParam(name = "to", required = false) @Nullable Instant to,
                               @RequestParam(name = "exerciseType", required = false) @Nullable ExerciseType exerciseType,
                               @RequestParam(name = "contentItemType", required = false) @Nullable ContentItemType contentItemType,
                               @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                               @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size);
}
```

- [ ] **Step 5: `ExercisesController.java`**

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.exercise.ExerciseHistoryService;
import com.blbulyandavbulyan.larm.exercise.ExerciseQueueService;
import com.blbulyandavbulyan.larm.exercise.ExerciseSessionService;
import com.blbulyandavbulyan.larm.exercise.SessionStartResult;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class ExercisesController implements ExercisesApi {

    private final ExerciseQueueService exerciseQueueService;

    private final ExerciseSessionService exerciseSessionService;

    private final ExerciseHistoryService exerciseHistoryService;

    private final ExerciseResponseMapper mapper;

    @Override
    public QueueResponse getQueue(DatabaseUserJwtAuthenticationToken auth, @Nullable ExerciseType exerciseType,
                                  @Nullable ContentItemType contentItemType, int page, int size) {
        return mapper.toQueueResponse(
                exerciseQueueService.getDueCards(auth.getUserId(), exerciseType, contentItemType, PageRequest.of(page, size)));
    }

    @Override
    public ResponseEntity<SessionResponse> startSession(DatabaseUserJwtAuthenticationToken auth, StartSessionRequest request) {
        SessionStartResult result = exerciseSessionService.startSession(auth.getUserId(), request.learningCardId());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(mapper.toResponse(result.session()));
    }

    @Override
    public SessionStatusResponse getSession(DatabaseUserJwtAuthenticationToken auth, UUID sessionId) {
        return mapper.toResponse(exerciseSessionService.getSession(auth.getUserId(), sessionId));
    }

    @Override
    public AttemptResponse submitAttempt(DatabaseUserJwtAuthenticationToken auth, UUID sessionId, AttemptRequest request) {
        return mapper.toResponse(exerciseSessionService.submitAttempt(auth.getUserId(), sessionId, mapper.toCommand(request)));
    }

    @Override
    public CompleteSessionResponse completeSession(DatabaseUserJwtAuthenticationToken auth, UUID sessionId) {
        return mapper.toResponse(exerciseSessionService.completeSession(auth.getUserId(), sessionId));
    }

    @Override
    public HistoryResponse getHistory(DatabaseUserJwtAuthenticationToken auth, @Nullable Instant from, @Nullable Instant to,
                                      @Nullable ExerciseType exerciseType, @Nullable ContentItemType contentItemType,
                                      int page, int size) {
        return mapper.toHistoryResponse(exerciseHistoryService.getHistory(auth.getUserId(), from, to, exerciseType,
                contentItemType, PageRequest.of(page, size)));
    }
}
```

- [ ] **Step 6: Compile, run `ArchitectureTest`, commit**

```bash
mvn -q compile && mvn test -Dtest=ArchitectureTest
git commit -m "feat(api): add exercises endpoints"
```

---

## Task 13: Test Data — Constants, SQL Fixtures, Expected JSON

Every IT prepares state with these fixtures and calls **only** the endpoint under test. The ids
are those from "Shared Test Data" at the top of this plan.

**Files:** create `src/test/java/com/blbulyandavbulyan/larm/srs/SrsTestData.java`, the SQL files
under `src/test/resources/sql-test-scripts/srs/`, and the JSON files under
`src/test/resources/expected-responses/srs/`.

- [ ] **Step 1: `SrsTestData.java`**

```java
package com.blbulyandavbulyan.larm.srs;

import java.util.UUID;

/**
 * Ids and fixture paths shared by the spaced repetition integration tests.
 */
public final class SrsTestData {

    public static final UUID DIALOGUE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    public static final UUID SPEAKER_A_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID SPEAKER_B_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    /** Line 1, speaker A, "Բարեւ ձեզ". */
    public static final UUID LINE_1_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    /** Line 2, speaker B. */
    public static final UUID LINE_2_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    /** Line 3, speaker A, "Ահա, խնդրեմ:". */
    public static final UUID LINE_3_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    public static final UUID TEST_USER_ID = UUID.fromString("5a5a5a5a-0000-0000-0000-000000000001");
    public static final UUID LEARNING_ITEM_ID = UUID.fromString("1a1a1a1a-0000-0000-0000-000000000001");
    public static final UUID OTHER_USER_LEARNING_ITEM_ID = UUID.fromString("1a1a1a1a-0000-0000-0000-000000000002");
    public static final UUID CARD_A_ID = UUID.fromString("2b2b2b2b-0000-0000-0000-0000000000aa");
    public static final UUID CARD_B_ID = UUID.fromString("2b2b2b2b-0000-0000-0000-0000000000bb");
    public static final UUID OTHER_USER_CARD_A_ID = UUID.fromString("2b2b2b2b-0000-0000-0000-0000000002aa");
    public static final UUID IN_PROGRESS_SESSION_ID = UUID.fromString("3c3c3c3c-0000-0000-0000-000000000001");
    public static final UUID COMPLETED_SESSION_ID = UUID.fromString("3c3c3c3c-0000-0000-0000-000000000002");
    public static final UUID OTHER_USER_SESSION_ID = UUID.fromString("3c3c3c3c-0000-0000-0000-000000000003");
    public static final UUID FIRST_ATTEMPT_ID = UUID.fromString("4d4d4d4d-0000-0000-0000-000000000001");

    /**
     * Paths of the SQL fixtures; usable in {@code @Sql}.
     */
    public interface Fixtures {
        String DIALOGUE = "/sql-test-scripts/insert-dialogue.sql";
        String TEST_USER = "/sql-test-scripts/srs/insert-test-user.sql";
        String ACTIVE_LEARNING_ITEM = "/sql-test-scripts/srs/insert-active-learning-item.sql";
        String EXCLUDED_LEARNING_ITEM = "/sql-test-scripts/srs/insert-excluded-learning-item.sql";
        String EXCLUDE_ACTIVE_LEARNING_ITEM = "/sql-test-scripts/srs/exclude-active-learning-item.sql";
        String OTHER_USER_LEARNING_ITEM = "/sql-test-scripts/srs/insert-other-user-learning-item.sql";
        String IN_PROGRESS_SESSION = "/sql-test-scripts/srs/insert-in-progress-session.sql";
        String FIRST_LINE_ATTEMPTS = "/sql-test-scripts/srs/insert-first-line-attempts.sql";
        String LAST_LINE_ATTEMPT = "/sql-test-scripts/srs/insert-last-line-attempt.sql";
        String COMPLETED_SESSION = "/sql-test-scripts/srs/insert-completed-session.sql";
        String OTHER_USER_SESSION = "/sql-test-scripts/srs/insert-other-user-session.sql";
    }

    private SrsTestData() {
    }
}
```

- [ ] **Step 2: SQL fixtures** (`src/test/resources/sql-test-scripts/srs/`)

`insert-test-user.sql`: the `@WithMockDatabaseUser` defaults, so `DatabaseUserJwtConverter` finds
this row instead of creating one with a random id (same pattern as `ChatControllerIT`):

```sql
INSERT INTO users (id, issuer, subject)
VALUES ('5a5a5a5a-0000-0000-0000-000000000001', 'https://test-issuer.com', 'test-subject-123');
```

`insert-active-learning-item.sql`: card A is due (08-14 < now 08-15), card B is in the future:

```sql
INSERT INTO learning_items (id, user_id, content_item_id, content_item_type, enrolled_at, excluded_at)
VALUES ('1a1a1a1a-0000-0000-0000-000000000001', '5a5a5a5a-0000-0000-0000-000000000001',
        '99999999-9999-9999-9999-999999999999', 'DIALOGUE', '2026-08-01T09:00:00Z', NULL);

INSERT INTO learning_cards (id, learning_item_id, exercise_type, variant_id, next_review_at, interval_days,
                            ease_factor, repetition_count, created_at, updated_at)
VALUES ('2b2b2b2b-0000-0000-0000-0000000000aa', '1a1a1a1a-0000-0000-0000-000000000001', 'TYPING',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2026-08-14T00:00:00Z', 6, 2.5, 2,
        '2026-08-01T09:00:00Z', '2026-08-08T00:00:00Z'),
       ('2b2b2b2b-0000-0000-0000-0000000000bb', '1a1a1a1a-0000-0000-0000-000000000001', 'TYPING',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-08-20T09:10:00Z', 6, 2.6, 2,
        '2026-08-01T09:00:00Z', '2026-08-14T09:10:00Z');
```

`insert-excluded-learning-item.sql`: identical to the previous file except
`excluded_at = '2026-08-10T12:00:00Z'`. (Never loaded together with it.)

`exclude-active-learning-item.sql`: for tests whose class-level fixtures already load the active item:

```sql
UPDATE learning_items SET excluded_at = '2026-08-10T12:00:00Z' WHERE id = '1a1a1a1a-0000-0000-0000-000000000001';
```

`insert-other-user-learning-item.sql`:

```sql
INSERT INTO users (id, issuer, subject)
VALUES ('5a5a5a5a-0000-0000-0000-000000000002', 'https://test-issuer.com', 'other-subject');

INSERT INTO learning_items (id, user_id, content_item_id, content_item_type, enrolled_at, excluded_at)
VALUES ('1a1a1a1a-0000-0000-0000-000000000002', '5a5a5a5a-0000-0000-0000-000000000002',
        '99999999-9999-9999-9999-999999999999', 'DIALOGUE', '2026-08-02T09:00:00Z', NULL);

INSERT INTO learning_cards (id, learning_item_id, exercise_type, variant_id, next_review_at, interval_days,
                            ease_factor, repetition_count, created_at, updated_at)
VALUES ('2b2b2b2b-0000-0000-0000-0000000002aa', '1a1a1a1a-0000-0000-0000-000000000002', 'TYPING',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2026-08-02T09:00:00Z', 0, 2.5, 0,
        '2026-08-02T09:00:00Z', '2026-08-02T09:00:00Z'),
       ('2b2b2b2b-0000-0000-0000-0000000002bb', '1a1a1a1a-0000-0000-0000-000000000002', 'TYPING',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-08-02T09:00:00Z', 0, 2.5, 0,
        '2026-08-02T09:00:00Z', '2026-08-02T09:00:00Z');
```

`insert-in-progress-session.sql` (card A; requires the active item):

```sql
INSERT INTO exercise_sessions (id, learning_card_id, user_id, aggregate_score, started_at, completed_at)
VALUES ('3c3c3c3c-0000-0000-0000-000000000001', '2b2b2b2b-0000-0000-0000-0000000000aa',
        '5a5a5a5a-0000-0000-0000-000000000001', NULL, '2026-08-15T09:50:00Z', NULL);
```

`insert-first-line-attempts.sql`: line 1 answered twice (quality 2, then 5):

```sql
INSERT INTO exercise_attempts (id, session_id, content_sub_item_id, answer_type, answer_text, used_hint,
                               raw_quality, quality, correct, evaluated_at)
VALUES ('4d4d4d4d-0000-0000-0000-000000000001', '3c3c3c3c-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc', 'TEXT', 'Բարև', FALSE, 2, 2, FALSE, '2026-08-15T09:51:00Z'),
       ('4d4d4d4d-0000-0000-0000-000000000002', '3c3c3c3c-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc', 'TEXT', 'Բարեւ ձեզ', FALSE, 5, 5, TRUE, '2026-08-15T09:52:00Z');
```

`insert-last-line-attempt.sql`: line 3 correct with a hint (raw 5, quality 4):

```sql
INSERT INTO exercise_attempts (id, session_id, content_sub_item_id, answer_type, answer_text, used_hint,
                               raw_quality, quality, correct, evaluated_at)
VALUES ('4d4d4d4d-0000-0000-0000-000000000003', '3c3c3c3c-0000-0000-0000-000000000001',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'TEXT', 'Ահա, խնդրեմ', TRUE, 5, 4, TRUE, '2026-08-15T09:53:00Z');
```

`insert-completed-session.sql` (card B; requires the active item):

```sql
INSERT INTO exercise_sessions (id, learning_card_id, user_id, aggregate_score, started_at, completed_at)
VALUES ('3c3c3c3c-0000-0000-0000-000000000002', '2b2b2b2b-0000-0000-0000-0000000000bb',
        '5a5a5a5a-0000-0000-0000-000000000001', 80, '2026-08-14T09:00:00Z', '2026-08-14T09:10:00Z');

INSERT INTO exercise_attempts (id, session_id, content_sub_item_id, answer_type, answer_text, used_hint,
                               raw_quality, quality, correct, evaluated_at)
VALUES ('4d4d4d4d-0000-0000-0000-000000000004', '3c3c3c3c-0000-0000-0000-000000000002',
        'dddddddd-dddd-dddd-dddd-dddddddddddd', 'TEXT', 'Բարեւ ձեզ, խնդրում եմ մեկ հայ', FALSE, 4, 4, FALSE,
        '2026-08-14T09:05:00Z');
```

`insert-other-user-session.sql` (requires the other user's item):

```sql
INSERT INTO exercise_sessions (id, learning_card_id, user_id, aggregate_score, started_at, completed_at)
VALUES ('3c3c3c3c-0000-0000-0000-000000000003', '2b2b2b2b-0000-0000-0000-0000000002aa',
        '5a5a5a5a-0000-0000-0000-000000000002', NULL, '2026-08-15T09:00:00Z', NULL);
```

- [ ] **Step 3: Expected responses** (`src/test/resources/expected-responses/srs/`). `%s` marks a
server-generated id that the test reads from the response and fills in with `String.formatted`.

`learning-items/enrolled-new.json`

```json
{
  "id": "%s",
  "contentItemId": "99999999-9999-9999-9999-999999999999",
  "contentItemType": "DIALOGUE",
  "enrolledAt": "2026-08-15T10:00:00Z",
  "excludedAt": null
}
```

`learning-items/active-learning-item.json`

```json
{
  "id": "1a1a1a1a-0000-0000-0000-000000000001",
  "contentItemId": "99999999-9999-9999-9999-999999999999",
  "contentItemType": "DIALOGUE",
  "enrolledAt": "2026-08-01T09:00:00Z",
  "excludedAt": null
}
```

`learning-items/cards.json`

```json
{
  "cards": [
    {
      "learningCardId": "2b2b2b2b-0000-0000-0000-0000000000aa",
      "learningItemId": "1a1a1a1a-0000-0000-0000-000000000001",
      "exerciseType": "TYPING",
      "variantId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "nextReviewAt": "2026-08-14T00:00:00Z",
      "intervalDays": 6,
      "repetitionCount": 2,
      "easeFactor": 2.5
    },
    {
      "learningCardId": "2b2b2b2b-0000-0000-0000-0000000000bb",
      "learningItemId": "1a1a1a1a-0000-0000-0000-000000000001",
      "exerciseType": "TYPING",
      "variantId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
      "nextReviewAt": "2026-08-20T09:10:00Z",
      "intervalDays": 6,
      "repetitionCount": 2,
      "easeFactor": 2.6
    }
  ]
}
```

`exercises/session-started.json`: speaker A's lines in dialogue order (line 1, line 3):

```json
{
  "sessionId": "%s",
  "learningCardId": "2b2b2b2b-0000-0000-0000-0000000000aa",
  "learningItemId": "1a1a1a1a-0000-0000-0000-000000000001",
  "contentItemId": "99999999-9999-9999-9999-999999999999",
  "contentItemType": "DIALOGUE",
  "exerciseType": "TYPING",
  "variantId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "subItemIds": ["cccccccc-cccc-cccc-cccc-cccccccccccc", "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"],
  "startedAt": "2026-08-15T10:00:00Z",
  "completedAt": null
}
```

`exercises/session-in-progress.json`: same as above with `"sessionId": "3c3c3c3c-0000-0000-0000-000000000001"`
and `"startedAt": "2026-08-15T09:50:00Z"`.

`exercises/session-status.json`

```json
{
  "session": {
    "sessionId": "3c3c3c3c-0000-0000-0000-000000000001",
    "learningCardId": "2b2b2b2b-0000-0000-0000-0000000000aa",
    "learningItemId": "1a1a1a1a-0000-0000-0000-000000000001",
    "contentItemId": "99999999-9999-9999-9999-999999999999",
    "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING",
    "variantId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "subItemIds": ["cccccccc-cccc-cccc-cccc-cccccccccccc", "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"],
    "startedAt": "2026-08-15T09:50:00Z",
    "completedAt": null
  },
  "attemptedSubItems": [
    {
      "contentSubItemId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
      "attempts": [
        {
          "attemptId": "4d4d4d4d-0000-0000-0000-000000000001",
          "answerType": "TEXT",
          "answerText": "Բարև",
          "usedHint": false,
          "quality": 2,
          "correct": false,
          "evaluatedAt": "2026-08-15T09:51:00Z"
        },
        {
          "attemptId": "4d4d4d4d-0000-0000-0000-000000000002",
          "answerType": "TEXT",
          "answerText": "Բարեւ ձեզ",
          "usedHint": false,
          "quality": 5,
          "correct": true,
          "evaluatedAt": "2026-08-15T09:52:00Z"
        }
      ]
    }
  ],
  "pendingSubItemIds": ["eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"]
}
```

`exercises/attempt-correct.json`

```json
{
  "attemptId": "%s",
  "contentSubItemId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "usedHint": false,
  "quality": 5,
  "correct": true,
  "expectedText": null
}
```

`exercises/attempt-wrong.json`

```json
{
  "attemptId": "%s",
  "contentSubItemId": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
  "usedHint": false,
  "quality": 1,
  "correct": false,
  "expectedText": "Ահա, խնդրեմ:"
}
```

`exercises/attempt-with-hint.json`

```json
{
  "attemptId": "%s",
  "contentSubItemId": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
  "usedHint": true,
  "quality": 4,
  "correct": true,
  "expectedText": null
}
```

`exercises/session-completed.json`. The line means are 3.5 (2, 5) and 4 (hint), so the session
mean is 3.75. That gives quality 4 and score 75. Card A goes from repetition 2 to 3, interval
`round(6 × 2.5) = 15`, ease `2.5 + 0.1 − 1 × 0.1 = 2.5`:

```json
{
  "sessionId": "3c3c3c3c-0000-0000-0000-000000000001",
  "completedAt": "2026-08-15T10:00:00Z",
  "aggregateScore": 75,
  "cardUpdate": {
    "previousIntervalDays": 6,
    "nextIntervalDays": 15,
    "nextReviewAt": "2026-08-30T10:00:00Z",
    "easeFactor": 2.5
  }
}
```

`exercises/queue-due-card.json`

```json
{
  "items": [
    {
      "learningCardId": "2b2b2b2b-0000-0000-0000-0000000000aa",
      "learningItemId": "1a1a1a1a-0000-0000-0000-000000000001",
      "contentItemId": "99999999-9999-9999-9999-999999999999",
      "contentItemType": "DIALOGUE",
      "exerciseType": "TYPING",
      "variantId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "nextReviewAt": "2026-08-14T00:00:00Z",
      "intervalDays": 6,
      "repetitionCount": 2
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

`exercises/history-completed-session.json`

```json
{
  "items": [
    {
      "sessionId": "3c3c3c3c-0000-0000-0000-000000000002",
      "learningCardId": "2b2b2b2b-0000-0000-0000-0000000000bb",
      "learningItemId": "1a1a1a1a-0000-0000-0000-000000000001",
      "contentItemId": "99999999-9999-9999-9999-999999999999",
      "contentItemType": "DIALOGUE",
      "exerciseType": "TYPING",
      "variantId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
      "startedAt": "2026-08-14T09:00:00Z",
      "completedAt": "2026-08-14T09:10:00Z",
      "aggregateScore": 80
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

`exercises/empty-page.json`

```json
{ "items": [], "page": 0, "size": 20, "totalElements": 0 }
```

- [ ] **Step 4: Commit** — `git commit -m "test: add spaced repetition fixtures and expected responses"`

---

## Task 14: `LearningItemsControllerIT`

**Files:** create `src/test/java/com/blbulyandavbulyan/larm/api/learning/LearningItemsControllerIT.java`.

- [ ] **Step 1: Write the test**

```java
package com.blbulyandavbulyan.larm.api.learning;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningItemRepository;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_B_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.DIALOGUE_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LEARNING_ITEM_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.OTHER_USER_LEARNING_ITEM_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.SPEAKER_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.SPEAKER_B_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER})
@WithMockDatabaseUser
class LearningItemsControllerIT extends BaseIT {

    interface RequestMapping {
        String LEARNING_ITEMS = "/learning-items";
        String LEARNING_ITEM = "/learning-items/{learningItemId}";
        String LEARNING_ITEM_CARDS = "/learning-items/{learningItemId}/cards";
    }

    private static final String ENROL_DIALOGUE_REQUEST = """
            {"contentItemId": "%s", "contentItemType": "DIALOGUE"}
            """.formatted(DIALOGUE_ID);

    @Autowired
    private LearningItemRepository learningItemRepository;

    @Autowired
    private LearningCardRepository learningCardRepository;

    @Test
    void enrol() throws Exception {
        String response = mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENROL_DIALOGUE_REQUEST))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID learningItemId = UUID.fromString(JsonPath.read(response, "$.id"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/learning-items/enrolled-new.json").formatted(learningItemId), response);

        assertThat(learningItemRepository.findById(learningItemId)).hasValueSatisfying(item -> {
            assertThat(item.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(item.getContentItemId()).isEqualTo(DIALOGUE_ID);
            assertThat(item.getEnrolledAt()).isEqualTo(NOW);
            assertThat(item.getExcludedAt()).isNull();
        });
        assertThat(learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(learningItemId))
                .extracting(LearningCard::getExerciseType, LearningCard::getVariantId, LearningCard::getNextReviewAt,
                        LearningCard::getIntervalDays, LearningCard::getEaseFactor, LearningCard::getRepetitionCount,
                        LearningCard::getCreatedAt, LearningCard::getUpdatedAt)
                .containsExactly(
                        tuple(ExerciseType.TYPING, SPEAKER_A_ID, NOW, 0, 2.5, 0, NOW, NOW),
                        tuple(ExerciseType.TYPING, SPEAKER_B_ID, NOW, 0, 2.5, 0, NOW, NOW));
    }

    @Test
    @Sql(Fixtures.EXCLUDED_LEARNING_ITEM)
    void enrol_whenExcluded() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENROL_DIALOGUE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/active-learning-item.json"),
                        JsonCompareMode.STRICT));

        assertThat(learningItemRepository.findById(LEARNING_ITEM_ID)).hasValueSatisfying(item ->
                assertThat(item.getExcludedAt()).isNull());
        assertThatCardProgressIsUnchanged();
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void enrol_whenAlreadyActive() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENROL_DIALOGUE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/active-learning-item.json"),
                        JsonCompareMode.STRICT));

        assertThat(learningItemRepository.count()).isEqualTo(1);
        assertThatCardProgressIsUnchanged();
    }

    @Test
    void enrol_whenContentItemNotFound() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentItemId": "00000000-0000-0000-0000-000000000000", "contentItemType": "DIALOGUE"}
                                """))
                .andExpect(status().isNotFound());

        assertThat(learningItemRepository.count()).isZero();
        assertThat(learningCardRepository.count()).isZero();
    }

    @Test
    void enrol_withMissingFields() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(learningItemRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void exclude() throws Exception {
        mockMvc.perform(delete(RequestMapping.LEARNING_ITEM, LEARNING_ITEM_ID))
                .andExpect(status().isNoContent());

        assertThat(learningItemRepository.findById(LEARNING_ITEM_ID)).hasValueSatisfying(item ->
                assertThat(item.getExcludedAt()).isEqualTo(NOW));
        assertThatCardProgressIsUnchanged();
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void exclude_whenOwnedByOtherUser() throws Exception {
        mockMvc.perform(delete(RequestMapping.LEARNING_ITEM, OTHER_USER_LEARNING_ITEM_ID))
                .andExpect(status().isNotFound());

        assertThat(learningItemRepository.findById(OTHER_USER_LEARNING_ITEM_ID)).hasValueSatisfying(item ->
                assertThat(item.getExcludedAt()).isNull());
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void findByContent() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEMS)
                        .param("contentItemId", DIALOGUE_ID.toString())
                        .param("contentItemType", "DIALOGUE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/active-learning-item.json"),
                        JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.EXCLUDED_LEARNING_ITEM)
    void findByContent_whenExcluded() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEMS)
                        .param("contentItemId", DIALOGUE_ID.toString())
                        .param("contentItemType", "DIALOGUE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void findByContent_whenNotEnrolled() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEMS)
                        .param("contentItemId", DIALOGUE_ID.toString())
                        .param("contentItemType", "DIALOGUE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getCards() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEM_CARDS, LEARNING_ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/cards.json"),
                        JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void getCards_whenOwnedByOtherUser() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEM_CARDS, OTHER_USER_LEARNING_ITEM_ID))
                .andExpect(status().isNotFound());
    }

    private void assertThatCardProgressIsUnchanged() {
        List<LearningCard> cards = learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(LEARNING_ITEM_ID);
        assertThat(cards)
                .extracting(LearningCard::getId, LearningCard::getRepetitionCount, LearningCard::getIntervalDays,
                        LearningCard::getEaseFactor)
                .containsExactly(tuple(CARD_A_ID, 2, 6, 2.5), tuple(CARD_B_ID, 2, 6, 2.6));
    }
}
```

`assertJsonEquals` is the `BaseIT` helper from Task 3. Tests that create resources compare the
whole body only after reading the generated id from it.

- [ ] **Step 2: Run** — `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=LearningItemsControllerIT` → all pass.

- [ ] **Step 3: Commit** — `git commit -m "test(api): add LearningItemsControllerIT"`

---

## Task 15: `ExerciseSessionControllerIT`

**Files:** create `src/test/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseSessionControllerIT.java`.

Every test starts with the dialogue, the test user and the active item (class-level `@Sql`).
Method-level fixtures add the session state the test needs.

- [ ] **Step 1: Write the test**

```java
package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Duration;
import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseAttemptRepository;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseSessionRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.ResultActions;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.COMPLETED_SESSION_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.IN_PROGRESS_SESSION_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LINE_1_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LINE_2_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LINE_3_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.OTHER_USER_CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.OTHER_USER_SESSION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER, Fixtures.ACTIVE_LEARNING_ITEM})
@WithMockDatabaseUser
class ExerciseSessionControllerIT extends BaseIT {

    interface RequestMapping {
        String SESSIONS = "/exercises/sessions";
        String SESSION = "/exercises/sessions/{sessionId}";
        String ATTEMPTS = "/exercises/sessions/{sessionId}/attempts";
        String COMPLETE = "/exercises/sessions/{sessionId}/complete";
    }

    @Autowired
    private ExerciseSessionRepository exerciseSessionRepository;

    @Autowired
    private ExerciseAttemptRepository exerciseAttemptRepository;

    @Autowired
    private LearningCardRepository learningCardRepository;

    // ======================== start ========================

    @Test
    void startSession() throws Exception {
        String response = startSessionFor(CARD_A_ID)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID sessionId = UUID.fromString(JsonPath.read(response, "$.sessionId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/session-started.json").formatted(sessionId), response);
        assertThat(exerciseSessionRepository.findAll())
                .singleElement()
                .satisfies(session -> {
                    assertThat(session.getId()).isEqualTo(sessionId);
                    assertThat(session.getStartedAt()).isEqualTo(NOW);
                    assertThat(session.getCompletedAt()).isNull();
                });
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void startSession_whenSessionInProgress() throws Exception {
        startSessionFor(CARD_A_ID)
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/exercises/session-in-progress.json"),
                        JsonCompareMode.STRICT));

        assertThat(exerciseSessionRepository.count()).isEqualTo(1);
    }

    @Test
    @Sql(Fixtures.EXCLUDE_ACTIVE_LEARNING_ITEM)
    void startSession_whenLearningItemExcluded() throws Exception {
        startSessionFor(CARD_A_ID).andExpect(status().isConflict());

        assertThat(exerciseSessionRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void startSession_whenCardOwnedByOtherUser() throws Exception {
        startSessionFor(OTHER_USER_CARD_A_ID).andExpect(status().isNotFound());

        assertThat(exerciseSessionRepository.count()).isZero();
    }

    // ======================== attempts ========================

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt() throws Exception {
        // ligature "և" and lower case are accepted for "Բարեւ ձեզ"
        String response = submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_1_ID, false, "բարև ձեզ")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(JsonPath.read(response, "$.attemptId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/attempt-correct.json").formatted(attemptId), response);
        assertThatStoredAttempt(attemptId, LINE_1_ID, "բարև ձեզ", false, 5, 5, true);
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withWrongAnswer() throws Exception {
        String response = submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_3_ID, false, "Ահա")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(JsonPath.read(response, "$.attemptId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/attempt-wrong.json").formatted(attemptId), response);
        assertThatStoredAttempt(attemptId, LINE_3_ID, "Ահա", false, 1, 1, false);
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withHint() throws Exception {
        String response = submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_3_ID, true, "Ահա, խնդրեմ")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(JsonPath.read(response, "$.attemptId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/attempt-with-hint.json").formatted(attemptId), response);
        assertThatStoredAttempt(attemptId, LINE_3_ID, "Ահա, խնդրեմ", true, 5, 4, true);
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_forOtherSpeakersLine() throws Exception {
        submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_2_ID, false, "Բարեւ ձեզ, խնդրում եմ մեկ հաց:")
                .andExpect(status().isConflict());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.COMPLETED_SESSION)
    void submitAttempt_whenSessionCompleted() throws Exception {
        submitTextAttempt(COMPLETED_SESSION_ID, LINE_2_ID, false, "Բարեւ ձեզ, խնդրում եմ մեկ հաց:")
                .andExpect(status().isConflict());

        assertThat(exerciseAttemptRepository.count()).isEqualTo(1);
    }

    @Test
    @Sql({Fixtures.OTHER_USER_LEARNING_ITEM, Fixtures.OTHER_USER_SESSION})
    void submitAttempt_whenSessionOwnedByOtherUser() throws Exception {
        submitTextAttempt(OTHER_USER_SESSION_ID, LINE_1_ID, false, "Բարեւ ձեզ")
                .andExpect(status().isNotFound());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withoutAnswerText() throws Exception {
        submitAttempt(IN_PROGRESS_SESSION_ID, """
                {"contentSubItemId": "%s", "usedHint": false, "answer": {"type": "TEXT"}}
                """.formatted(LINE_1_ID))
                .andExpect(status().isBadRequest());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withUnknownAnswerType() throws Exception {
        submitAttempt(IN_PROGRESS_SESSION_ID, """
                {"contentSubItemId": "%s", "usedHint": false, "answer": {"type": "AUDIO", "assetId": "%s"}}
                """.formatted(LINE_1_ID, UUID.randomUUID()))
                .andExpect(status().isBadRequest());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    // ======================== recovery ========================

    @Test
    @Sql({Fixtures.IN_PROGRESS_SESSION, Fixtures.FIRST_LINE_ATTEMPTS})
    void getSession() throws Exception {
        mockMvc.perform(get(RequestMapping.SESSION, IN_PROGRESS_SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/exercises/session-status.json"),
                        JsonCompareMode.STRICT));
    }

    @Test
    @Sql({Fixtures.OTHER_USER_LEARNING_ITEM, Fixtures.OTHER_USER_SESSION})
    void getSession_whenSessionOwnedByOtherUser() throws Exception {
        mockMvc.perform(get(RequestMapping.SESSION, OTHER_USER_SESSION_ID))
                .andExpect(status().isNotFound());
    }

    // ======================== complete ========================

    @Test
    @Sql({Fixtures.IN_PROGRESS_SESSION, Fixtures.FIRST_LINE_ATTEMPTS, Fixtures.LAST_LINE_ATTEMPT})
    void completeSession() throws Exception {
        mockMvc.perform(post(RequestMapping.COMPLETE, IN_PROGRESS_SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/exercises/session-completed.json"),
                        JsonCompareMode.STRICT));

        assertThat(exerciseSessionRepository.findById(IN_PROGRESS_SESSION_ID)).hasValueSatisfying(session -> {
            assertThat(session.getCompletedAt()).isEqualTo(NOW);
            assertThat(session.getAggregateScore()).isEqualTo(75);
        });
        assertThat(learningCardRepository.findById(CARD_A_ID)).hasValueSatisfying(card -> {
            assertThat(card.getRepetitionCount()).isEqualTo(3);
            assertThat(card.getIntervalDays()).isEqualTo(15);
            assertThat(card.getEaseFactor()).isEqualTo(2.5);
            assertThat(card.getNextReviewAt()).isEqualTo(NOW.plus(Duration.ofDays(15)));
            assertThat(card.getUpdatedAt()).isEqualTo(NOW);
        });
    }

    @Test
    @Sql({Fixtures.IN_PROGRESS_SESSION, Fixtures.FIRST_LINE_ATTEMPTS})
    void completeSession_whenSubItemsPending() throws Exception {
        mockMvc.perform(post(RequestMapping.COMPLETE, IN_PROGRESS_SESSION_ID))
                .andExpect(status().isConflict());

        assertThat(exerciseSessionRepository.findById(IN_PROGRESS_SESSION_ID)).hasValueSatisfying(session ->
                assertThat(session.getCompletedAt()).isNull());
        assertThat(learningCardRepository.findById(CARD_A_ID)).hasValueSatisfying(card ->
                assertThat(card.getRepetitionCount()).isEqualTo(2));
    }

    @Test
    @Sql(Fixtures.COMPLETED_SESSION)
    void completeSession_whenAlreadyCompleted() throws Exception {
        mockMvc.perform(post(RequestMapping.COMPLETE, COMPLETED_SESSION_ID))
                .andExpect(status().isConflict());

        assertThat(exerciseSessionRepository.findById(COMPLETED_SESSION_ID)).hasValueSatisfying(session ->
                assertThat(session.getAggregateScore()).isEqualTo(80));
    }

    // ======================== helpers ========================

    private ResultActions startSessionFor(UUID learningCardId) throws Exception {
        return mockMvc.perform(post(RequestMapping.SESSIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"learningCardId": "%s"}
                        """.formatted(learningCardId)));
    }

    private ResultActions submitTextAttempt(UUID sessionId, UUID subItemId, boolean usedHint, String text) throws Exception {
        return submitAttempt(sessionId, """
                {"contentSubItemId": "%s", "usedHint": %s, "answer": {"type": "TEXT", "text": "%s"}}
                """.formatted(subItemId, usedHint, text));
    }

    private ResultActions submitAttempt(UUID sessionId, String body) throws Exception {
        return mockMvc.perform(post(RequestMapping.ATTEMPTS, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertThatStoredAttempt(UUID attemptId, UUID subItemId, String answerText, boolean usedHint,
                                         int rawQuality, int quality, boolean correct) {
        assertThat(exerciseAttemptRepository.findById(attemptId)).get()
                .extracting(ExerciseAttempt::getContentSubItemId, ExerciseAttempt::getAnswerType, ExerciseAttempt::getAnswerText,
                        ExerciseAttempt::isUsedHint, ExerciseAttempt::getRawQuality, ExerciseAttempt::getQuality,
                        ExerciseAttempt::isCorrect, ExerciseAttempt::getEvaluatedAt)
                .containsExactly(subItemId, AnswerType.TEXT, answerText, usedHint, rawQuality, quality, correct, NOW);
    }
}
```

- [ ] **Step 2: Run** — `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=ExerciseSessionControllerIT` → all pass.

- [ ] **Step 3: Commit** — `git commit -m "test(api): add ExerciseSessionControllerIT"`

---

## Task 16: Queue and History ITs

**Files:** create `src/test/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseQueueControllerIT.java`
and `ExerciseHistoryControllerIT.java`.

- [ ] **Step 1: `ExerciseQueueControllerIT`**

The first test calls the endpoint **without filters**. That covers the `@Nullable` repository
parameters. The fixture has one due card (A) and one future card (B), so the same assertion
proves due cards appear and future cards do not.

```java
package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER, Fixtures.OTHER_USER_LEARNING_ITEM})
@WithMockDatabaseUser
class ExerciseQueueControllerIT extends BaseIT {

    private static final String QUEUE = "/exercises/queue";

    private static final String DUE_CARD = "/expected-responses/srs/exercises/queue-due-card.json";

    private static final String EMPTY_PAGE = "/expected-responses/srs/exercises/empty-page.json";

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getQueue() throws Exception {
        mockMvc.perform(get(QUEUE))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(DUE_CARD), JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getQueue_filteredByExerciseType() throws Exception {
        mockMvc.perform(get(QUEUE).param("exerciseType", "TYPING"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(DUE_CARD), JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getQueue_filteredByContentItemType() throws Exception {
        mockMvc.perform(get(QUEUE).param("contentItemType", "DIALOGUE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(DUE_CARD), JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.EXCLUDED_LEARNING_ITEM)
    void getQueue_whenLearningItemExcluded() throws Exception {
        mockMvc.perform(get(QUEUE))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(EMPTY_PAGE), JsonCompareMode.STRICT));
    }

    @Test
    void getQueue_withInvalidPageSize() throws Exception {
        mockMvc.perform(get(QUEUE).param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
```

The other user's due cards are loaded in every test (class-level fixture). Every expected queue
contains only the test user's cards, so the user filter is verified as well.

- [ ] **Step 2: `ExerciseHistoryControllerIT`**

```java
package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER, Fixtures.ACTIVE_LEARNING_ITEM, Fixtures.IN_PROGRESS_SESSION,
        Fixtures.COMPLETED_SESSION})
@WithMockDatabaseUser
class ExerciseHistoryControllerIT extends BaseIT {

    private static final String HISTORY = "/exercises/history";

    private static final String COMPLETED = "/expected-responses/srs/exercises/history-completed-session.json";

    private static final String EMPTY_PAGE = "/expected-responses/srs/exercises/empty-page.json";

    @Test
    void getHistory() throws Exception {
        // the in-progress session is not part of the history
        mockMvc.perform(get(HISTORY))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(COMPLETED), JsonCompareMode.STRICT));
    }

    @Test
    void getHistory_filteredByCompletionTime() throws Exception {
        mockMvc.perform(get(HISTORY).param("from", "2026-08-14T09:10:01Z"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(EMPTY_PAGE), JsonCompareMode.STRICT));
    }

    @Test
    void getHistory_filteredByExerciseTypeAndContentItemType() throws Exception {
        mockMvc.perform(get(HISTORY).param("exerciseType", "TYPING").param("contentItemType", "DIALOGUE")
                        .param("to", "2026-08-14T09:10:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(COMPLETED), JsonCompareMode.STRICT));
    }
}
```

- [ ] **Step 3: Run** — `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test='ExerciseQueueControllerIT,ExerciseHistoryControllerIT'` → all pass.

- [ ] **Step 4: Commit** — `git commit -m "test(api): add queue and history ITs"`

---

## Task 17: Extend `JpaEntitiesIT`

**Files:** modify `src/test/java/com/blbulyandavbulyan/larm/dao/entities/JpaEntitiesIT.java`.

- [ ] **Step 1: Extend the class-level `@Sql`.** Do not add a per-method `@Sql`; the existing
class-level annotation already runs before every test:

```java
@Sql(scripts = {
        "/sql-test-scripts/drop-all-data-after-test.sql",
        "/sql-test-scripts/insert-dialogue.sql",
        "/sql-test-scripts/srs/insert-test-user.sql",
        "/sql-test-scripts/srs/insert-active-learning-item.sql",
        "/sql-test-scripts/srs/insert-in-progress-session.sql",
        "/sql-test-scripts/srs/insert-first-line-attempts.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
```

- [ ] **Step 2: Add the tests** (same pattern as the existing `dialogue*` tests; import
`com.blbulyandavbulyan.larm.srs.SrsTestData` constants statically)

```java
    @Test
    void learningItemToString() {
        LearningItem item = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));

        assertThatCode(() -> assertThat(item.toString()).contains("LearningItem{id=" + LEARNING_ITEM_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void learningItemEquals() {
        LearningItem first = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));
        LearningItem second = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new LearningItem());
            assertThat(new LearningItem()).isNotEqualTo(new LearningItem());
        }).doesNotThrowAnyException();
    }

    @Test
    void learningItemHashCode() {
        LearningItem item = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));

        assertThatCode(() -> assertThat(item.hashCode()).isEqualTo("LearningItem".hashCode())).doesNotThrowAnyException();
    }

    @Test
    void learningCardToString() {
        LearningCard card = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));

        assertThatCode(() -> assertThat(card.toString())
                .contains("LearningCard{id=" + CARD_A_ID)
                .contains("(LAZY LOADING)"))
                .doesNotThrowAnyException();
    }

    @Test
    void learningCardEquals() {
        LearningCard first = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));
        LearningCard second = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new LearningCard());
            assertThat(new LearningCard()).isNotEqualTo(new LearningCard());
        }).doesNotThrowAnyException();
    }

    @Test
    void learningCardHashCode() {
        LearningCard card = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));

        assertThatCode(() -> assertThat(card.hashCode()).isEqualTo("LearningCard".hashCode())).doesNotThrowAnyException();
    }

    @Test
    void exerciseSessionToString() {
        ExerciseSession session = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));

        assertThatCode(() -> assertThat(session.toString())
                .contains("ExerciseSession{id=" + IN_PROGRESS_SESSION_ID)
                .contains("(LAZY LOADING)"))
                .doesNotThrowAnyException();
    }

    @Test
    void exerciseSessionEquals() {
        ExerciseSession first = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));
        ExerciseSession second = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new ExerciseSession());
            assertThat(new ExerciseSession()).isNotEqualTo(new ExerciseSession());
        }).doesNotThrowAnyException();
    }

    @Test
    void exerciseSessionHashCode() {
        ExerciseSession session = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));

        assertThatCode(() -> assertThat(session.hashCode()).isEqualTo("ExerciseSession".hashCode())).doesNotThrowAnyException();
    }

    @Test
    void exerciseAttemptToString() {
        ExerciseAttempt attempt = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));

        assertThatCode(() -> assertThat(attempt.toString())
                .contains("ExerciseAttempt{id=" + FIRST_ATTEMPT_ID)
                .contains("(LAZY LOADING)"))
                .doesNotThrowAnyException();
    }

    @Test
    void exerciseAttemptEquals() {
        ExerciseAttempt first = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));
        ExerciseAttempt second = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new ExerciseAttempt());
            assertThat(new ExerciseAttempt()).isNotEqualTo(new ExerciseAttempt());
        }).doesNotThrowAnyException();
    }

    @Test
    void exerciseAttemptHashCode() {
        ExerciseAttempt attempt = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));

        assertThatCode(() -> assertThat(attempt.hashCode()).isEqualTo("ExerciseAttempt".hashCode())).doesNotThrowAnyException();
    }
```

- [ ] **Step 3: Run** — `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=JpaEntitiesIT` → all pass.

- [ ] **Step 4: Commit** — `git commit -m "test(entities): cover spaced repetition entities in JpaEntitiesIT"`

---

## Task 18: Full Verification

- [ ] **Step 1:** `mvn clean verify` → `BUILD SUCCESS`. This runs checkstyle and PMD at compile,
  unit tests, ITs and `ArchitectureTest`.
- [ ] **Step 2:** `mvn test-compile org.pitest:pitest-maven:mutationCoverage` → mutation score ≥ 80 %
  (the threshold CI enforces). Likely survivors are in `TypingAnswerEvaluator.qualityFor`
  boundaries; add evaluator tests for similarities just above and below each threshold if so.
- [ ] **Step 3:** Start the app (`docker compose up -d`, profile `local`) and check that
  `/swagger-ui/index.html` shows `AnswerRequest` as `oneOf` with discriminator `type`.
- [ ] **Step 4:** Commit any fixes.

---

## Self-Review

### Known limitations (accepted)

- Two parallel **first** enrolments of the same dialogue hit the unique constraint, so the second
  one gets a 500. Re-enrolment is safe. A double tap on "enrol" is unlikely; map
  `DataIntegrityViolationException` to 409 if it ever shows up.
- Queue and history items carry ids only; the client loads titles and speaker names from
  `GET /dialogues/{id}` (spec §13).
