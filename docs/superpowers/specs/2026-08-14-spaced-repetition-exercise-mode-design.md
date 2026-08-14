# Spaced Repetition Exercise Mode - Technical Design

## 1. Overview

This spec describes the backend implementation for the interactive Spaced Repetition System (SRS)
exercise mode (GitHub Issue #49). The system introduces stateful exercise sessions with per-step
server-side evaluation and immediate feedback, driven by the SM-2 spaced repetition algorithm.

The design is intentionally generic: it supports any type of learnable content (dialogues,
vocabulary, grammar rules) and any exercise type (typing, speaking, multiple-choice) without
requiring API changes when new variants are added.

---

## 2. Domain Model

### 2.1 Glossary

| Term | Meaning |
|---|---|
| **LearningItem** | A user-owned enrolment record — "I am studying *this* content item". Separate from the global content. The user can soft-delete it without touching global data. |
| **LearningCard** | The SM-2 scheduling state for one `(user, LearningItem, exerciseType)` triple. Tracks interval, ease factor, repetition count, next review date. |
| **ExerciseSession** | One study attempt of a LearningCard, from creation to completion. Exists server-side from the moment it is started. |
| **ExerciseAttempt** | One evaluated sub-item within a session (e.g. one dialogue phrase). Persisted the moment it is evaluated — this is what enables crash recovery. |
| **SessionAttachment** | An uploaded binary file (audio, etc.) linked to a session, used when the answer cannot be sent as plain text. |

### 2.2 Enums

```
ContentItemType  — extensible: DIALOGUE | VOCABULARY | GRAMMAR_RULE | …
ExerciseType     — extensible: TYPING | SPEAKING | MULTIPLE_CHOICE | …
```

---

## 3. Database Schema

### `learning_items`
Tracks the user's intent to study a specific content item.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | own identity, used by all downstream tables |
| `user_id` | UUID FK → users | owner |
| `content_item_id` | UUID | reference to the content (dialogue, vocab set, etc.); NOT a hard FK — content lives in its own tables |
| `content_item_type` | VARCHAR | maps to `ContentItemType` enum |
| `enrolled_at` | TIMESTAMPTZ | when the user enrolled |
| `excluded_at` | TIMESTAMPTZ NULL | soft-delete; NULL = active, non-NULL = excluded |

Constraint: `UNIQUE (user_id, content_item_id, content_item_type)`.

> `excluded_at` is used instead of hard delete so that `learning_cards` (and their SR progress)
> survive across exclude/re-enroll cycles. Re-enrolling clears `excluded_at`.

### `learning_cards`
SM-2 scheduling state per `(learning_item, exercise_type)`.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `learning_item_id` | UUID FK → learning_items | |
| `exercise_type` | VARCHAR | maps to `ExerciseType` enum |
| `next_review_at` | TIMESTAMPTZ | when this card is next due |
| `interval_days` | INTEGER | current SR interval |
| `ease_factor` | REAL | SM-2 E-factor; default 2.5 |
| `repetition_count` | INTEGER | number of successful reviews; default 0 |

Constraint: `UNIQUE (learning_item_id, exercise_type)`.

Auto-created on the first `POST /exercises/sessions/{id}/complete` for a given pair.

### `exercise_sessions`
One study attempt of a LearningCard.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `learning_card_id` | UUID FK → learning_cards | |
| `started_at` | TIMESTAMPTZ | |
| `completed_at` | TIMESTAMPTZ NULL | NULL = in-progress; non-NULL = sealed |

`completed_at IS NULL` is the single source of truth for "in-progress" state. No separate status column.

### `exercise_attempts`
One evaluated sub-item inside a session. Persisted at evaluation time.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `session_id` | UUID FK → exercise_sessions | |
| `content_sub_item_id` | UUID | opaque reference to the sub-item (e.g. `dialogue_phrases.id`) |
| `used_hint` | BOOLEAN | |
| `quality` | INTEGER | 0–5 (SM-2 scale), computed server-side |
| `evaluated_at` | TIMESTAMPTZ | |

Constraint: `UNIQUE (session_id, content_sub_item_id)` — a sub-item can only be evaluated once per session.

### `session_attachments`
Uploaded binary files (e.g. recorded audio for SPEAKING exercises) linked to a session.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `session_id` | UUID FK → exercise_sessions | |
| `content_type` | VARCHAR | MIME type (e.g. `audio/wav`, `audio/ogg`) |
| `stored_object_key` | VARCHAR | key in `ObjectStorageService` |
| `uploaded_at` | TIMESTAMPTZ | |

---

## 4. Session Lifecycle

```
[Not started]
     │
     ▼  POST /exercises/sessions
[IN_PROGRESS]  ←──── GET /exercises/sessions/{id}/status  (crash recovery, any time)
     │
     │  POST /exercises/sessions/{id}/attachments  (optional, for binary answers)
     │  POST /exercises/sessions/{id}/evaluate     (one call per sub-item, or batched)
     │  (repeat until all sub-items evaluated)
     │
     ▼  POST /exercises/sessions/{id}/complete
[COMPLETED]
  (completed_at set; SM-2 run; learning_card updated)
```

`POST /complete` is rejected with `409 Conflict` if any sub-items from the content item
have not been evaluated yet. SM-2 runs exactly once, on the complete set of attempts.

---

## 5. API Contract

All endpoints require the authenticated user (resolved from JWT via existing `DatabaseUserJwtConverter`).

### 5.1 LearningItem Endpoints (`/learning-items`)

**`POST /learning-items`** — Enrol a content item for learning.

```json
// Request
{ "contentItemId": "uuid", "contentItemType": "DIALOGUE" }

// Response 201
{ "id": "uuid", "contentItemId": "uuid", "contentItemType": "DIALOGUE",
  "enrolledAt": "…", "excludedAt": null }
```

Idempotent: if already enrolled (and not excluded), returns the existing record.
If previously excluded, clears `excluded_at` and returns the existing record.

**`DELETE /learning-items/{learningItemId}`** — Soft-delete (exclude) a learning item.

Response: `204 No Content`. Sets `excluded_at = now()`. Does not touch the `learning_card`.

**`GET /learning-items/{learningItemId}/card?exerciseType={type}`** — Get SR state.

Returns the `LearningCard` for the given `(learningItem, exerciseType)` pair.
Returns `404` if the card has never been created (item never completed an exercise in this mode).

```json
// Response 200
{ "learningCardId": "uuid", "learningItemId": "uuid", "exerciseType": "TYPING",
  "nextReviewAt": "…", "intervalDays": 8, "repetitionCount": 5, "easeFactor": 2.36 }
```

### 5.2 Exercise Queue (`/exercises/queue`)

**`GET /exercises/queue`** — What's due today?

Returns `LearningCard`s with `next_review_at <= now()`, excluding items where
`learning_items.excluded_at IS NOT NULL`. Ordered by `next_review_at ASC`.

Query params: `exerciseType` (optional), `contentItemType` (optional), `page`, `size`.

```json
// Response 200
{
  "content": [{
    "learningCardId": "uuid", "learningItemId": "uuid",
    "contentItemId": "uuid", "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING", "nextReviewAt": "…",
    "intervalDays": 3, "repetitionCount": 4
  }],
  "totalDue": 12, "page": 0, "size": 10
}
```

### 5.3 Session Endpoints (`/exercises/sessions`)

**`POST /exercises/sessions`** — Start a session.

```json
// Request
{ "learningCardId": "uuid" }

// Response 201
{ "sessionId": "uuid", "learningCardId": "uuid",
  "contentItemId": "uuid", "contentItemType": "DIALOGUE",
  "exerciseType": "TYPING", "startedAt": "…", "completedAt": null }
```

Idempotent: if a session for the same `learningCardId` is already in progress
(`completed_at IS NULL`), returns that session (no duplicate created).

---

**`POST /exercises/sessions/{sessionId}/attachments`** — Upload a binary answer.

`Content-Type: multipart/form-data`. Form field: `file` (raw binary).

```json
// Response 201
{ "attachmentId": "uuid" }
```

Stored via existing `ObjectStorageService`. Scoped to the session for lifecycle management.

---

**`POST /exercises/sessions/{sessionId}/evaluate`** — Evaluate one or more attempts.

Accepts a list of attempts. Each attempt provides **either** `answerText` (for TYPING/MCQ) **or**
`attachmentId` (for SPEAKING) — never both. `usedHint` is a factual flag.

The server evaluates the answer (text comparison or audio analysis), applies hint penalty,
persists the `ExerciseAttempt`, and returns immediate per-attempt feedback.

```json
// Request
{
  "attempts": [
    { "contentSubItemId": "uuid", "usedHint": false, "answerText": "Ողջույն" },
    { "contentSubItemId": "uuid", "usedHint": true, "attachmentId": "uuid" }
  ]
}

// Response 200
{
  "results": [
    { "contentSubItemId": "uuid", "quality": 5, "usedHint": false,
      "correct": true, "expectedText": null },
    { "contentSubItemId": "uuid", "quality": 2, "usedHint": true,
      "correct": false, "expectedText": "Լավ եմ, շնորհակալ" }
  ]
}
```

`expectedText` is non-null only when `correct = false` — shown to the user as a correction.
`quality` has already had the hint penalty applied (penalty is server-side policy, not client).
Returns `409 Conflict` if a `contentSubItemId` has already been evaluated in this session.

---

**`GET /exercises/sessions/{sessionId}/status`** — Session recovery.

Returns the current state of a session for crash recovery. The client calls this on
resume to know exactly which sub-items are done and which are next.

```json
// Response 200
{
  "sessionId": "uuid", "learningCardId": "uuid",
  "contentItemId": "uuid", "contentItemType": "DIALOGUE",
  "exerciseType": "TYPING", "startedAt": "…", "completedAt": null,
  "evaluatedAttempts": [
    { "contentSubItemId": "uuid", "quality": 5, "usedHint": false,
      "correct": true, "evaluatedAt": "…" }
  ],
  "pendingSubItemIds": ["uuid-phrase-2", "uuid-phrase-3"]
}
```

`pendingSubItemIds` = all sub-items of the content item minus already-evaluated ones.

---

**`POST /exercises/sessions/{sessionId}/complete`** — Seal session and run SM-2.

No request body. Validates all sub-items are evaluated (returns `409` if not).
Computes aggregate score from persisted `ExerciseAttempt` records, runs SM-2,
updates the `LearningCard`, and sets `completed_at`.

```json
// Response 200
{
  "sessionId": "uuid", "completedAt": "…", "aggregateScore": 78,
  "cardUpdate": {
    "previousIntervalDays": 3, "nextIntervalDays": 8,
    "nextReviewAt": "…", "easeFactor": 2.36
  }
}
```

### 5.4 History (`/exercises/history`)

**`GET /exercises/history`** — Analytics feed of completed sessions.

Query params: `from`, `to` (Instant), `exerciseType`, `contentItemType`, `page`, `size`.

```json
// Response 200
{
  "content": [{
    "sessionId": "uuid", "learningItemId": "uuid",
    "contentItemId": "uuid", "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING", "completedAt": "…", "aggregateScore": 78
  }],
  "page": 0, "size": 20, "totalElements": 47
}
```

---

## 6. SM-2 Algorithm

Run once per session on `POST /complete`. Input is the list of persisted `ExerciseAttempt.quality`
values for the session. Aggregate quality = average of all attempt qualities (integer, rounded).

```
Given aggregateQuality (0–5):

if aggregateQuality >= 3:
    repetitionCount++
    if repetitionCount == 1:  intervalDays = 1
    elif repetitionCount == 2: intervalDays = 6
    else: intervalDays = round(previousIntervalDays * easeFactor)
    easeFactor = max(1.3, easeFactor + 0.1 - (5 - aggregateQuality) * (0.08 + (5 - aggregateQuality) * 0.02))
else:
    repetitionCount = 0
    intervalDays = 1

nextReviewAt = now() + intervalDays (days)
```

Hint penalty applied per-attempt during evaluate: `quality = max(0, computedQuality - 1)` when
`usedHint = true`. The penalty value (1) is a server-side configuration constant, not client-supplied.

---

## 7. Module Structure (Spring Modulith)

Following existing project conventions:

| Package | Responsibility |
|---|---|
| `larm.learning` | `LearningItem` entity, repo, `LearningItemService` (enrol/exclude), `LearningItemController` |
| `larm.exercise` | `LearningCard`, `ExerciseSession`, `ExerciseAttempt`, `SessionAttachment` entities, repos |
| `larm.exercise` | `ExerciseSessionService` (session lifecycle, evaluation, completion), `ExerciseQueueService`, `ExerciseHistoryService` |
| `larm.exercise` | `ExerciseController` (session, queue, history endpoints) |
| `larm.exercise.srs` | `Sm2Algorithm` — pure function, no Spring dependencies; takes attempt qualities, returns updated card state |
| `larm.exercise.evaluation` | `AnswerEvaluator` interface + `TypingAnswerEvaluator` implementation (text comparison, quality scoring) |

The `Sm2Algorithm` is a pure stateless component — it takes inputs and returns outputs. It has no
Spring annotations and is unit-testable without a context.

---

## 8. Testing Strategy

Follows the project's testing conventions from `GEMINI.md`.

### Integration Tests (`*IT.java`)

- **`LearningItemControllerIT`**: Full stack (controller → service → DB). Testcontainers PostgreSQL.
  Covers: enrol (201), re-enrol after exclude (200), exclude (204), get card state (200 / 404).

- **`ExerciseControllerIT`**: Full stack. Covers the complete happy path:
  start session → upload attachment → evaluate (TYPING + SPEAKING paths) → get status → complete.
  Also covers: idempotent session start, `409` on duplicate evaluate, `409` on complete with pending items.

- **`ExerciseQueueControllerIT`**: Verify due items appear; excluded items do not; filters work.

- Mock boundary: `ObjectStorageService` (binary storage) is mocked at the interface level.
  No other internal layers are mocked.

### Unit Tests

- **`Sm2AlgorithmTest`**: Pure unit test. Parametrized over quality scores 0–5. Verifies:
  interval progression (1 → 6 → round(6 * easeFactor)), ease factor bounds (min 1.3),
  reset on quality < 3. No Spring context.

- **`TypingAnswerEvaluatorTest`**: Verifies quality scoring for exact match (5), minor typo (3-4),
  major mismatch (0-1), hint-penalty application.

- **`JpaEntitiesIT`**: Must cover `LearningItem`, `LearningCard`, `ExerciseSession`,
  `ExerciseAttempt`, `SessionAttachment` — verifying `toString`/`equals`/`hashCode`
  do not trigger `LazyInitializationException` or `StackOverflowError`.

---

## 9. Error Handling

Uses existing project `RestControllerAdvice` / `EntityNotFoundException` conventions.

| Situation | HTTP status |
|---|---|
| `learningItemId` or `learningCardId` not found | `404 Not Found` |
| `learningCardId` belongs to a different user | `403 Forbidden` |
| `sessionId` not found | `404 Not Found` |
| Attempt to evaluate an already-evaluated sub-item | `409 Conflict` |
| Attempt to `complete` with pending sub-items | `409 Conflict` |
| Attempt to modify a completed session | `409 Conflict` |
| Attachment MIME type not permitted | `400 Bad Request` |
