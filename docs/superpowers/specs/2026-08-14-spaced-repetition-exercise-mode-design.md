# Spaced Repetition Exercise Mode - Technical Design

## 1. Overview

This spec describes the backend implementation for the interactive Spaced Repetition System (SRS)
exercise mode (GitHub Issue #49). The system introduces stateful exercise sessions with per-step
server-side evaluation and immediate feedback, driven by the SM-2 spaced repetition algorithm.

The design is intentionally generic: it supports any type of learnable content (dialogues,
vocabulary, grammar rules) and any exercise type (typing, speaking, multiple-choice) without
requiring API changes when new variants are added. However, **only the TYPING exercise type
is in scope for Issue #49**. Infrastructure for other types (attachment upload) is built now,
but evaluation logic for non-TYPING types returns `501 Not Implemented` until specified.

---

## 2. Domain Model

### 2.1 Glossary

| Term | Meaning |
|---|---|
| **LearningItem** | A user-owned enrolment record — "I am studying *this* content item". Separate from the global content. The user can soft-delete it without touching global data. |
| **LearningCard** | The SM-2 scheduling state for one `(user, LearningItem, exerciseType)` triple. Tracks interval, ease factor, repetition count, next review date. One card is auto-created per `ExerciseType` at enrolment time. |
| **ExerciseSession** | One study attempt of a LearningCard, from creation to completion. Exists server-side from the moment it is started. |
| **ExerciseAttempt** | One evaluated sub-item within a session (e.g. one dialogue phrase). Persisted the moment it is evaluated — this is what enables crash recovery. |
| **SessionAttachment** | An uploaded binary file (audio, etc.) linked to a session, used when the answer cannot be sent as plain text. Infrastructure built for Issue #49; evaluation logic for audio is not yet implemented. |

### 2.2 Enums

```
ContentItemType  — extensible: DIALOGUE | VOCABULARY | GRAMMAR_RULE | …
ExerciseType     — extensible: TYPING | SPEAKING | MULTIPLE_CHOICE | …
```

Both are stored as `VARCHAR` with a `CHECK` constraint in the database. The Java enum provides
type safety at the application layer; the constraint provides it at the DB layer.

### 2.3 Content Ownership

Dialogues (and other content items) are **global content** — they do not belong to a specific user.
Any authenticated user may enrol any content item from the global catalogue. There is no ownership
validation at enrolment time. This is an intentional design decision.

### 2.4 Hint Integrity

`usedHint` is **client-asserted**. The server applies a quality penalty when `true` but does not
independently verify that a hint was displayed. This is an intentional design decision: this is a
personal learning tool and the user only cheats themselves.

### 2.5 Open Design Question (Speaker Dimension)

The original GitHub Issue #49 describes a "roleplaying" model where the user practices as one
specific speaker in a dialogue. The current spec does not model this: sessions evaluate
sub-items without speaker scoping. **This is an acknowledged gap.** The session creation
request is designed to accept an optional `speakerId` in a future iteration without breaking
existing clients, but the mechanics of speaker-scoped evaluation are not specified here.

---

## 3. Database Schema

### `learning_items`
Tracks the user's intent to study a specific content item.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | own identity, used by all downstream tables |
| `user_id` | UUID FK → users | owner |
| `content_item_id` | UUID | reference to the content (dialogue, vocab set, etc.); soft FK — content lives in its own tables |
| `content_item_type` | VARCHAR | maps to `ContentItemType` enum; CHECK constraint enforced |
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
| `exercise_type` | VARCHAR | maps to `ExerciseType` enum; CHECK constraint enforced |
| `next_review_at` | TIMESTAMPTZ | when this card is next due |
| `interval_days` | INTEGER | current SR interval; default 0 |
| `ease_factor` | DOUBLE PRECISION | SM-2 E-factor; default 2.5. `DOUBLE PRECISION` (64-bit) used over `REAL` (32-bit) to avoid float drift across hundreds of reviews. |
| `repetition_count` | INTEGER | number of successful reviews; default 0 |
| `created_at` | TIMESTAMPTZ | when the card was created |
| `updated_at` | TIMESTAMPTZ | last time SM-2 was applied; useful for debugging unexpected interval jumps |

Constraint: `UNIQUE (learning_item_id, exercise_type)`.

**Auto-created at `POST /learning-items`**: one `LearningCard` is created per supported
`ExerciseType` with `next_review_at = now()` and SM-2 defaults, so the item immediately
appears in the queue. No separate card-creation step.

### `exercise_sessions`
One study attempt of a LearningCard.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `learning_card_id` | UUID FK → learning_cards | |
| `user_id` | UUID FK → users | **denormalized** from `learning_items` for O(1) ownership checks |
| `aggregate_score` | INTEGER NULL | populated at `POST /complete`; NULL while in-progress |
| `started_at` | TIMESTAMPTZ | |
| `completed_at` | TIMESTAMPTZ NULL | NULL = in-progress; non-NULL = sealed |

`completed_at IS NULL` is the single source of truth for "in-progress" state. No separate status column.

> `user_id` is denormalized here to avoid a 3-table join (`exercise_sessions → learning_cards → learning_items`)
> on every authorization check. Every session-scoped endpoint (`POST /evaluate`, `GET /status`,
> `POST /complete`, `POST /attachments`) validates ownership with a single
> `WHERE id = ? AND user_id = ?` check on this table.

### `exercise_attempts`
One evaluated sub-item inside a session. Persisted at evaluation time.

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `session_id` | UUID FK → exercise_sessions | |
| `content_sub_item_id` | UUID | opaque reference to the sub-item (e.g. `dialogue_phrases.id`) |
| `used_hint` | BOOLEAN | client-asserted; server applies penalty but does not verify independently |
| `quality` | INTEGER | 0–5 (SM-2 scale), computed server-side after hint penalty |
| `evaluated_at` | TIMESTAMPTZ | |

TODO DUMB CONSTRAINT! USER CAN REDO THE 'sub item' to get better score! IT DOES NOT MAKE SENSE TO WAIT UNTIL FULL EXERCISE FINISHES!
FRONTEND WILL ALSO WONT ACCEPT 'too bad' answers, tehre will be 'minimal pass score'!
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

**Attachment lifecycle**: Max file size is `10 MB` (enforced by Spring multipart config). MIME type
is validated against a server-side allowlist (`audio/wav`, `audio/ogg`, `audio/mp4`, `audio/webm`).
Attachments for completed sessions are retained. Attachments belonging to sessions abandoned for
more than 7 days (no `completed_at`, `started_at < now() - 7 days`) are eligible for cleanup by a
scheduled maintenance task (out of scope for this issue — noted for future work).

### Required Indexes (Flyway migration)

```sql
-- Core queue query: most frequently executed query in the system
CREATE INDEX idx_learning_items_user_excluded
    ON learning_items(user_id, excluded_at);

CREATE INDEX idx_learning_cards_item_review
    ON learning_cards(learning_item_id, next_review_at);

-- Session ownership check (every session-scoped endpoint)
CREATE INDEX idx_exercise_sessions_user
    ON exercise_sessions(user_id);

-- Attempt lookup for status / completeness check
CREATE INDEX idx_exercise_attempts_session
    ON exercise_attempts(session_id);
```

---

## 4. Content Item Resolver (Cross-Module Contract)

The `larm.exercise` module needs to ask: *"What are the evaluatable sub-items for content item X?"*
This knowledge lives in other modules (e.g. `larm.phrase` knows about `dialogue_phrases`).

To avoid a direct module dependency from `larm.exercise` → `larm.phrase` (which would couple
exercise to every content type and violate Spring Modulith boundaries), a **`ContentItemResolver`
interface** is defined in `larm.exercise` and implemented by each content module:

```java
// Defined in larm.exercise — owned by the exercise module
public interface ContentItemResolver {

    ContentItemType supportedType();

    /**
     * Returns the ordered list of evaluatable sub-item IDs for the given content item.
     * For DIALOGUE, this is all dialogue_phrase IDs for the given dialogueId.
     */
    List<UUID> resolveSubItemIds(UUID contentItemId);

    /**
     * Returns the expected answer text for a given sub-item.
     * For DIALOGUE / TYPING: navigates DialoguePhrase → Phrase → phrase (Armenian text).
     */
    String resolveExpectedAnswer(UUID contentSubItemId);

    /**
     * Validates that the given sub-item ID belongs to the given content item.
     * Used during evaluate to prevent clients submitting arbitrary UUIDs.
     */
    boolean belongsToContentItem(UUID contentSubItemId, UUID contentItemId);
}
```

`larm.phrase` registers a `DialogueContentItemResolver implements ContentItemResolver`
bean that handles `ContentItemType.DIALOGUE`. The `ExerciseSessionService` in `larm.exercise`
injects all `ContentItemResolver` beans and dispatches by type.

### Answer Resolution for DIALOGUE / TYPING

The Armenian expected answer is **not** a field on `dialogue_phrases`. The actual join chain is:

```
content_sub_item_id (= dialogue_phrases.id)
    → dialogue_phrases.phrase_id
    → phrases.phrase          ← Armenian text lives here
```

`DialogueContentItemResolver.resolveExpectedAnswer(UUID subItemId)` performs this join.
The `TypingAnswerEvaluator` receives the resolved Armenian text string and compares it to the
client's `answerText`.

---

## 5. Session Lifecycle

```
[Not started]
     │
     ▼  POST /exercises/sessions
[IN_PROGRESS]  ←──── GET /exercises/sessions/{id}/status  (crash recovery, any time)
     │
     │  POST /exercises/sessions/{id}/attachments  (for SPEAKING — infrastructure only; 501 for now)
     │  POST /exercises/sessions/{id}/evaluate     (one call per sub-item, or batched)
     │  (repeat until all sub-items evaluated)
     │
     ▼  POST /exercises/sessions/{id}/complete
[COMPLETED]
  (completed_at set; aggregate_score computed; SM-2 run; learning_card updated)
```

- `POST /evaluate` with `attachmentId` returns `501 Not Implemented` (SPEAKING not in scope for #49).
- `POST /complete` returns `409 Conflict` if any sub-items from the content item have not been evaluated.
- `POST /complete` uses `SELECT ... FOR UPDATE` on the `exercise_sessions` row to prevent concurrent
  completions. If `completed_at` is already set when the lock is acquired, returns `409 Conflict`.
- `POST /evaluate` racing with a concurrent `POST /complete` is protected by the session row lock
  in `complete` and the unique constraint in `exercise_attempts`.

---

## 6. API Contract

All endpoints require the authenticated user resolved from JWT via `DatabaseUserJwtConverter`.
No `/api/` prefix — consistent with all existing endpoints in this project.

### 6.1 LearningItem Endpoints (`/learning-items`)

**`POST /learning-items`** — Enrol a content item.

Creates a `LearningItem` and **one `LearningCard` per supported `ExerciseType`** with
`next_review_at = now()`. The new cards immediately appear in the SR queue.

If the item was previously excluded, `excluded_at` is cleared and existing cards are preserved
(SR progress survives the exclude/re-enroll cycle).

```json
// Request
{ "contentItemId": "uuid", "contentItemType": "DIALOGUE" }

// Response 201 — newly enrolled
// Response 200 — re-enrolled (excluded_at cleared)
{
  "id": "uuid",
  "contentItemId": "uuid",
  "contentItemType": "DIALOGUE",
  "enrolledAt": "2026-08-15T09:00:00Z",
  "excludedAt": null
}
```

**`DELETE /learning-items/{learningItemId}`** — Soft-delete (exclude).

Sets `excluded_at = now()`. Global content is untouched. `LearningCard`s are preserved.

Response: `204 No Content`.

**`GET /learning-items?contentItemId={uuid}&contentItemType={type}`** — Look up by content.

Allows clients to find a user's `LearningItem` for a known piece of content (e.g. the user
is browsing their dialogue list and wants to check enrolment status or start an exercise).
Returns `404` if the user has not enrolled this content item (or it is excluded).

```json
// Response 200
{
  "id": "uuid",
  "contentItemId": "uuid",
  "contentItemType": "DIALOGUE",
  "enrolledAt": "2026-08-15T09:00:00Z",
  "excludedAt": null
}
```
TODO what's the point of this endpoint ?????
**`GET /learning-items/{learningItemId}/card?exerciseType={type}`** — Get SR card state.

Returns `404` if no card exists for the given `exerciseType` (e.g. unrecognised type).

```json
// Response 200
{
  "learningCardId": "uuid",
  "learningItemId": "uuid",
  "exerciseType": "TYPING",
  "nextReviewAt": "2026-08-23T00:00:00Z",
  "intervalDays": 8,
  "repetitionCount": 5,
  "easeFactor": 2.36
}
```

### 6.2 SR Queue (`/exercises/queue`)

**`GET /exercises/queue`** — What's due today?

Returns `LearningCard`s with `next_review_at <= now()`, excluding items where
`learning_items.excluded_at IS NOT NULL`. Ordered by `next_review_at ASC`.

Query params: `exerciseType` (optional), `contentItemType` (optional), `page`, `size`.

```json
// Response 200
{
  "content": [{
    "learningCardId": "uuid",
    "learningItemId": "uuid",
    "contentItemId": "uuid",
    "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING",
    "nextReviewAt": "2026-08-15T00:00:00Z",
    "intervalDays": 3,
    "repetitionCount": 4
  }],
  "totalDue": 12,
  "page": 0,
  "size": 10
}
```

### 6.3 Session Endpoints (`/exercises/sessions`)

**`POST /exercises/sessions`** — Start a session.

Idempotent: if a session for the same `learningCardId` is already in progress
(`completed_at IS NULL`), returns that existing session (no duplicate created).

```json
// Request
{ "learningCardId": "uuid" }

// Response 201
{
  "sessionId": "uuid",
  "learningCardId": "uuid",
  "contentItemId": "uuid",
  "contentItemType": "DIALOGUE",
  "exerciseType": "TYPING",
  "startedAt": "2026-08-15T10:00:00Z",
  "completedAt": null
}
```

---

**`POST /exercises/sessions/{sessionId}/attachments`** — Upload a binary answer.
TODO SHOULD NOT BE IMPLEMENTED YET ! SHOULD RETURN 501 Not Implemeneted for now, because OUT OF SCOPE!

Infrastructure only for Issue #49. Accepts `multipart/form-data` with a `file` field.
Max file size: `10 MB`. Allowed MIME types: `audio/wav`, `audio/ogg`, `audio/mp4`, `audio/webm`.

```json
// Response 201
{ "attachmentId": "uuid" }
// Response 400 — unsupported MIME type or file too large
// Response 403 — session does not belong to authenticated user
```

---

**`POST /exercises/sessions/{sessionId}/evaluate`** — Evaluate attempts (core gameplay loop).

Each attempt provides **either** `answerText` (for TYPING) **or** `attachmentId` (for SPEAKING —
returns `501 Not Implemented` in Issue #49). Never both. `usedHint` is a factual client-asserted flag.

The server evaluates each answer, applies hint penalty, persists `ExerciseAttempt`, and returns
immediate per-attempt feedback.

**Atomicity**: the entire request is transactional. If any attempt in the array fails validation
(e.g. `content_sub_item_id` already evaluated, or sub-item does not belong to the content item),
the whole request is rejected with `409 Conflict` and no attempts are persisted.

```json
// Request
{
  "attempts": [
    { "contentSubItemId": "uuid-phrase-1", "usedHint": false, "answerText": "Ողջույն" },
    { "contentSubItemId": "uuid-phrase-2", "usedHint": true, "answerText": "Ինչպե՞ս ես" }
  ]
}

// Response 200
{
  "results": [
    {
      "contentSubItemId": "uuid-phrase-1",
      "quality": 5,
      "usedHint": false,
      "correct": true,
      "expectedText": null
    },
    {
      "contentSubItemId": "uuid-phrase-2",
      "quality": 3,
      "usedHint": true,
      "correct": false,
      "expectedText": "Ինչպե՞ս եք"
    }
  ]
}

// Response 409 — duplicate contentSubItemId, or sub-item doesn't belong to content item
// Response 403 — session does not belong to authenticated user
// Response 501 — attachmentId provided (SPEAKING not implemented)
```

`expectedText` is non-null only when `correct = false`. Populated from the resolved Armenian text
(`dialogue_phrases → phrases.phrase`) via `ContentItemResolver`.
`quality` has already had the hint penalty applied (`max(0, rawQuality - 1)` when `usedHint = true`).

---

**`GET /exercises/sessions/{sessionId}/status`** — Session recovery.

Returns the full current state of a session. Client calls this on app resume to reconstruct
exercise UI state without data loss.

`pendingSubItemIds` = all sub-item IDs from `ContentItemResolver.resolveSubItemIds()` minus
those already present in `exercise_attempts` for this session.

```json
// Response 200
{
  "sessionId": "uuid",
  "learningCardId": "uuid",
  "contentItemId": "uuid",
  "contentItemType": "DIALOGUE",
  "exerciseType": "TYPING",
  "startedAt": "2026-08-15T10:00:00Z",
  "completedAt": null,
  "evaluatedAttempts": [
    {
      "contentSubItemId": "uuid-phrase-1",
      "quality": 5,
      "usedHint": false,
      "correct": true,
      "evaluatedAt": "2026-08-15T10:02:00Z"
    }
  ],
  "pendingSubItemIds": ["uuid-phrase-2", "uuid-phrase-3"]
}
```

---

**`POST /exercises/sessions/{sessionId}/complete`** — Seal session and run SM-2.

No request body. Uses `SELECT ... FOR UPDATE` on `exercise_sessions` to prevent concurrent
completions. Returns `409 Conflict` if any sub-items remain unevaluated, or if the session
is already completed.

Computes `aggregateScore = round((avgQuality / 5.0) * 100)` from all persisted `ExerciseAttempt`
quality values. Runs SM-2. Updates the `LearningCard`. Stores `aggregateScore` on the session row.

```json
// Response 200
{
  "sessionId": "uuid",
  "completedAt": "2026-08-15T10:12:00Z",
  "aggregateScore": 78,
  "cardUpdate": {
    "previousIntervalDays": 3,
    "nextIntervalDays": 8,
    "nextReviewAt": "2026-08-23T00:00:00Z",
    "easeFactor": 2.36
  }
}
// Response 409 — pending sub-items remain, or session already completed
// Response 403 — session does not belong to authenticated user
```

### 6.4 History (`/exercises/history`)

**`GET /exercises/history`** — Analytics feed of completed sessions.

`aggregateScore` is read from the stored column (computed once at `POST /complete`,
not recomputed on every history query).

Query params: `from`, `to` (Instant), `exerciseType`, `contentItemType`, `page`, `size`.

```json
// Response 200
{
  "content": [{
    "sessionId": "uuid",
    "learningItemId": "uuid",
    "contentItemId": "uuid",
    "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING",
    "completedAt": "2026-08-15T10:12:00Z",
    "aggregateScore": 78
  }],
  "page": 0,
  "size": 20,
  "totalElements": 47
}
```

---

## 7. SM-2 Algorithm

Run once per session on `POST /complete`. Input: the list of persisted `ExerciseAttempt.quality`
values for the session.

```
aggregateQuality = round(average of all attempt.quality values)

if aggregateQuality >= 3:
    repetitionCount++
    if repetitionCount == 1:   intervalDays = 1
    elif repetitionCount == 2: intervalDays = 6
    else:                      intervalDays = round(previousIntervalDays * easeFactor)
    easeFactor = max(1.3, easeFactor + 0.1 - (5 - aggregateQuality) * (0.08 + (5 - aggregateQuality) * 0.02))
else:
    repetitionCount = 0
    intervalDays = 1

nextReviewAt = now() + intervalDays days
updated_at   = now()
```

**Hint penalty** (applied per-attempt during evaluate, before storage):
`quality = max(0, rawQuality - 1)` when `usedHint = true`.
The penalty constant (1) is a server-side configuration value.

**aggregateScore** (stored on session, shown to user):
`aggregateScore = round((avgQuality / 5.0) * 100)` — a 0–100 percentage of maximum possible quality.

---

## 8. Module Structure (Spring Modulith)

| Package | Responsibility |
|---|---|
| `larm.learning` | `LearningItem` + `LearningCard` entities, repos, `LearningItemService` (enrol/exclude/lookup), `LearningItemController` |
| `larm.exercise` | `ExerciseSession`, `ExerciseAttempt`, `SessionAttachment` entities + repos |
| `larm.exercise` | `ExerciseSessionService` (session lifecycle, evaluation, completion), `ExerciseQueueService`, `ExerciseHistoryService` |
| `larm.exercise` | `ExerciseController` (session, queue, history endpoints) |
| `larm.exercise` | `ContentItemResolver` interface (contract for cross-module sub-item resolution) |
| `larm.exercise.srs` | `Sm2Algorithm` — pure function, no Spring dependencies; takes quality list, returns updated card state |
| `larm.exercise.evaluation` | `AnswerEvaluator` interface + `TypingAnswerEvaluator` (text comparison, quality scoring) |
| `larm.phrase` | `DialogueContentItemResolver implements ContentItemResolver` — registered as a Spring bean; resolves sub-items and expected answers for `DIALOGUE` type via `DialoguePhrase → Phrase` join |

`larm.exercise` must **not** directly import from `larm.phrase` or `larm.dao.entities`. All
cross-module data access goes through the `ContentItemResolver` interface.

---

## 9. Existing Draft Code to Delete

The branch contains draft API interfaces that **conflict** with this spec and must be removed
before implementation begins. Do not attempt to reconcile them — delete and replace:

| File to delete | Replaced by |
|---|---|
| `larm.api.learning.plan.LearningPlanApi` | `LearningItemController` in `larm.learning` |
| `larm.api.learning.plan.AddLearningItemRequest` | New `EnrolLearningItemRequest` record |
| `larm.api.learning.plan.LearningPlanResponse` | `ExerciseQueueResponse` |
| `larm.api.learning.plan.LearningItemType` | `ContentItemType` enum in `larm.learning` |
| `larm.api.exercises.ExerciseApi` | `ExerciseController` in `larm.exercise` |
| `larm.api.exercises.TrackProgressRequest` | `EvaluateAttemptsRequest` + `CompleteSessionRequest` |
| `larm.api.exercises.DialogueSpeakerRoleplayingTypingRequest` | `EvaluateAttemptsRequest` |
| `larm.api.exercises.PhraseTypingAttempt` | `AttemptRequest` record |
| `larm.api.exercises.ExerciseType` | `ExerciseType` enum in `larm.exercise` |

---

## 10. Testing Strategy

Follows the project's testing conventions from `GEMINI.md`.

### Integration Tests (`*IT.java`)

- **`LearningItemControllerIT`**: Full stack (controller → service → DB). Testcontainers PostgreSQL.
  Covers: enrol (201 + cards auto-created), re-enrol after exclude (200, cards preserved),
  exclude (204), lookup by contentItemId (200 / 404), get card state (200 / 404).

- **`ExerciseControllerIT`**: Full stack. Covers the complete happy path:
  start session → evaluate (TYPING) → get status (resumed state) → complete.
  Also covers: idempotent session start, `409` on duplicate evaluate, `409` on complete with
  pending items, `409` on concurrent complete (via sequential calls), `501` on attachmentId evaluate,
  `403` when session belongs to a different user.

- **`ExerciseQueueControllerIT`**: Due items appear; excluded items do not; filters by
  `exerciseType` and `contentItemType` work; items with `next_review_at > now()` do not appear.

- Mock boundary: `ObjectStorageService` (binary storage) is mocked at the interface level.
  No other internal layers are mocked.

### Unit Tests

- **`Sm2AlgorithmTest`**: Parametrized over quality 0–5. Verifies: interval progression
  (1 → 6 → round(6 * easeFactor)), ease factor bounds (min 1.3), reset on quality < 3,
  `updated_at` is set. No Spring context.

- **`TypingAnswerEvaluatorTest`**: Exact match → quality 5; minor typo → quality 3–4;
  major mismatch → quality 0–1; `usedHint = true` reduces quality by 1 (floor 0).

- **`JpaEntitiesIT`**: Must cover `LearningItem`, `LearningCard`, `ExerciseSession`,
  `ExerciseAttempt`, `SessionAttachment` — verifying `toString`/`equals`/`hashCode`
  do not trigger `LazyInitializationException` or `StackOverflowError`.

---

## 11. Error Handling

Uses existing project `RestControllerAdvice` / `EntityNotFoundException` conventions.

| Situation | HTTP status |
|---|---|
| `learningItemId`, `learningCardId`, or `sessionId` not found | `404 Not Found` |
| Session / card / item belongs to a different user | `403 Forbidden` |
| Attempt to evaluate an already-evaluated `contentSubItemId` | `409 Conflict` |
| `contentSubItemId` does not belong to the session's content item | `409 Conflict` |
| Attempt to `complete` with pending sub-items | `409 Conflict` |
| Attempt to modify a completed session | `409 Conflict` |
| Attachment MIME type not in allowlist | `400 Bad Request` |
| Attachment exceeds 10 MB | `400 Bad Request` |
| `attachmentId` provided in evaluate request (SPEAKING not implemented) | `501 Not Implemented` |
