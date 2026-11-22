# Spaced Repetition Exercise Mode - Technical Design

## 1. Overview

This spec describes the backend for the interactive Spaced Repetition System (SRS) exercise mode
(GitHub Issue #49). The user enrols a saved dialogue, picks a speaker to role-play, and types that
speaker's lines. Every answer is evaluated on the server with immediate feedback. When the session
is completed, the SM-2 algorithm schedules the next review.

**In scope for #49:** content type `DIALOGUE`, exercise type `TYPING`, answer type `TEXT`.

**Designed for, not built:** spoken answers (`SPEAKING` exercise, `AUDIO` answers) and other content
types (vocabulary, grammar). §12 lists exactly what adding them touches. The rule used throughout:
anything a client sends or receives must already be shaped so that these additions are **new
values / new optional fields**, never a changed field.

---

## 2. Domain Model

### 2.1 Glossary

| Term | Meaning |
|---|---|
| **Content item** | A piece of global learnable content, e.g. a dialogue. Not owned by any user. |
| **Variant** | A sub-scope of a content item that is practised separately. For `DIALOGUE` the variant is the **speaker the user plays**. Content types without variants use `null`. |
| **Sub-item** | One thing the user must answer inside a session. For `DIALOGUE` it is one `dialogue_phrases` row (one line of the dialogue). |
| **LearningItem** | User-owned enrolment: "I am studying *this* content item". Can be soft-excluded. |
| **LearningCard** | SM-2 scheduling state for one `(LearningItem, exerciseType, variant)`. A two-speaker dialogue enrolled for `TYPING` gets two cards with independent schedules. |
| **ExerciseSession** | One study attempt of a LearningCard, from start to completion. |
| **ExerciseAttempt** | One evaluated answer for a sub-item inside a session. Retries are allowed; every attempt is kept. |

### 2.2 Enums

```
ContentItemType — DIALOGUE            (later: VOCABULARY, GRAMMAR_RULE, …)
ExerciseType    — TYPING              (later: SPEAKING, …)
AnswerType      — TEXT                (later: AUDIO, …)
```

All three are stored as `VARCHAR(32)` with a **named** `CHECK` constraint, so that adding a value
is `ALTER TABLE … DROP CONSTRAINT … ; ALTER TABLE … ADD CONSTRAINT …` in a new Flyway migration.
They are mapped with `@Enumerated(EnumType.STRING)` and live in `larm.dao.entities` next to the
existing `StorageProvider` enum.

### 2.3 Why a card per speaker

Typing speaker A's lines and typing speaker B's lines are different exercises. If they shared one
schedule, the user would always pick the easier role and the schedule would describe neither.
So each speaker gets their own card, and a session only asks for the lines of that card's speaker.
The card stores the speaker as an opaque `variant_id`, so the exercise code never knows that it
is a speaker.

### 2.4 Why a separate exercise type for speech (future)

Speaking is a different skill from spelling. When voice answers arrive they get their own
`ExerciseType.SPEAKING` and therefore their own cards and schedule. A good spoken session must not
postpone the typing review, and the reverse. This keeps "how is an answer evaluated" a pure
function of the card's `exerciseType`.

### 2.5 Content Ownership

Dialogues are **global content**. Any authenticated user may enrol any existing content item.
Enrolment checks that the content item exists (404 otherwise), but not who created it.

### 2.6 Hint Integrity

`usedHint` is **client-asserted**. The server applies a quality penalty when it is `true` but does
not verify that a hint was shown. This is a personal learning tool; the user only cheats themselves.

---

## 3. Database Schema (Flyway `V4__create_srs_tables.sql`)

### `learning_items`

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `user_id` | UUID NOT NULL FK → users | owner |
| `content_item_id` | UUID NOT NULL | soft reference (e.g. `dialogues.id`); no FK because it points to different tables per type |
| `content_item_type` | VARCHAR(32) NOT NULL | `CHECK` constraint `chk_learning_items_content_item_type` |
| `enrolled_at` | TIMESTAMPTZ NOT NULL | |
| `excluded_at` | TIMESTAMPTZ NULL | soft-delete; NULL = active |

`UNIQUE (user_id, content_item_id, content_item_type)`.

Soft delete keeps `learning_cards` (and SR progress) across exclude/re-enrol cycles.

### `learning_cards`

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `learning_item_id` | UUID NOT NULL FK → learning_items | |
| `exercise_type` | VARCHAR(32) NOT NULL | `CHECK` constraint `chk_learning_cards_exercise_type` |
| `variant_id` | UUID NULL | for `DIALOGUE`: `dialogue_speakers.id`; NULL for content without variants |
| `next_review_at` | TIMESTAMPTZ NOT NULL | |
| `interval_days` | INTEGER NOT NULL DEFAULT 0 | |
| `ease_factor` | DOUBLE PRECISION NOT NULL DEFAULT 2.5 | `CHECK (ease_factor >= 1.3)` |
| `repetition_count` | INTEGER NOT NULL DEFAULT 0 | |
| `created_at` | TIMESTAMPTZ NOT NULL | |
| `updated_at` | TIMESTAMPTZ NOT NULL | last SM-2 update |

`UNIQUE NULLS NOT DISTINCT (learning_item_id, exercise_type, variant_id)`: without
`NULLS NOT DISTINCT`, two cards with a NULL variant would not conflict. Needs PostgreSQL 15+
(docker-compose uses 16, Testcontainers uses 17).

### `exercise_sessions`

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `learning_card_id` | UUID NOT NULL FK → learning_cards | |
| `user_id` | UUID NOT NULL FK → users | denormalized for single-table ownership checks |
| `aggregate_score` | INTEGER NULL | 0–100, set on completion |
| `started_at` | TIMESTAMPTZ NOT NULL | |
| `completed_at` | TIMESTAMPTZ NULL | NULL = in progress (single source of truth) |

`CREATE UNIQUE INDEX uq_exercise_sessions_one_in_progress ON exercise_sessions (learning_card_id)
WHERE completed_at IS NULL;` This guarantees at most one open session per card, even if the
application-level lock (§5) were bypassed.

### `exercise_attempts`

| column | type | notes |
|---|---|---|
| `id` | UUID PK | |
| `session_id` | UUID NOT NULL FK → exercise_sessions | |
| `content_sub_item_id` | UUID NOT NULL | opaque (for `DIALOGUE`: `dialogue_phrases.id`) |
| `answer_type` | VARCHAR(32) NOT NULL | `CHECK` constraint `chk_exercise_attempts_answer_type` |
| `answer_text` | TEXT NULL | the typed text for `TEXT` answers (`CHECK`: required when `answer_type = 'TEXT'`) |
| `used_hint` | BOOLEAN NOT NULL | |
| `raw_quality` | INTEGER NOT NULL | 0–5, evaluator output before hint penalty |
| `quality` | INTEGER NOT NULL | 0–5, after hint penalty; used by SM-2 |
| `correct` | BOOLEAN NOT NULL | answer matched exactly (independent of hint) |
| `evaluated_at` | TIMESTAMPTZ NOT NULL | |

The answer itself is stored so that attempts can be re-scored when the evaluator improves, and so
that odd scores can be debugged. A future `AUDIO` answer adds an `answer_media_id` column (§12).

### Indexes

```sql
CREATE INDEX idx_learning_cards_item_review ON learning_cards (learning_item_id, next_review_at);
CREATE INDEX idx_exercise_sessions_user_completed ON exercise_sessions (user_id, completed_at);
CREATE INDEX idx_exercise_attempts_session ON exercise_attempts (session_id);
```

`learning_items (user_id, …)` lookups are already covered by the unique constraint's index.

---

## 4. Content Item Resolver (content-type plug-in point)

The exercise code must work for any content type without knowing its tables. Each content type
provides one `ContentItemResolver` bean (interface in `larm.learning`):

```java
public interface ContentItemResolver {

    ContentItemType supportedType();

    /** Exercise types that can be practised on this content type (one card each, per variant). */
    Set<ExerciseType> supportedExerciseTypes();

    boolean exists(UUID contentItemId);

    /** Variants practised separately; an empty list means "no variants" (one card, variant = null). */
    List<UUID> resolveVariantIds(UUID contentItemId);

    /** Ordered sub-items the user must answer for the given variant (all sub-items when variant is null). */
    List<UUID> resolveSubItemIds(UUID contentItemId, @Nullable UUID variantId);

    /** Expected answer for a sub-item; empty if the sub-item does not belong to the content item. */
    Optional<ExpectedAnswer> resolveExpectedAnswer(UUID contentItemId, UUID contentSubItemId);
}

public record ExpectedAnswer(String text, @Nullable String transcription) { }
```

`DialogueContentItemResolver` (in `larm.dialogue`) implements it for `DIALOGUE`:

| method | dialogue implementation |
|---|---|
| `resolveVariantIds` | speaker ids of the dialogue, ordered by `created_at, id` |
| `resolveSubItemIds` | `dialogue_phrases.id` of that speaker, ordered by `order_index` |
| `resolveExpectedAnswer` | `dialogue_phrases → phrases.phrase` / `phrases.transcription`, filtered by dialogue id |

`ExpectedAnswer` is internal, so a future evaluator that needs more (e.g. the reference TTS audio)
adds a field here without any API change.

---

## 5. Session Lifecycle

```
[Not started]
     │  POST /exercises/sessions                     (idempotent: returns the open session if any)
     ▼
[IN PROGRESS] ── GET  /exercises/sessions/{id}            (recovery, any time)
     │         ── POST /exercises/sessions/{id}/attempts   (one answer per call; retries allowed)
     │  POST /exercises/sessions/{id}/complete      (all sub-items must have ≥ 1 attempt)
     ▼
[COMPLETED]    completed_at + aggregate_score set; SM-2 applied to the card
```

Concurrency:

- **Start** locks the card row (`SELECT … FOR UPDATE`) before looking for an open session, so
  two parallel starts return the same session. The partial unique index is the safety net.
- **Attempt** and **complete** lock the session row. An attempt can never land on a session that
  is being completed, and a second complete sees `completed_at` and returns `409`.

---

## 6. API Contract

All endpoints require the JWT user (`DatabaseUserJwtAuthenticationToken`). No `/api/` prefix.
Resources of another user are reported as **404**, exactly like missing ones. Every lookup is
`WHERE id = ? AND user_id = ?`, and 404 does not reveal that the id exists.

### 6.1 Learning items (`/learning-items`)

**`POST /learning-items`** — enrol a content item.

```json
{ "contentItemId": "uuid", "contentItemType": "DIALOGUE" }
```

- Content item does not exist → `404`.
- New → `201`, creates the item and one card per `supportedExerciseType × variant`, each with
  `next_review_at = now`, so it is due immediately.
- Already enrolled (active or excluded) → `200`. Clears `excluded_at` and **creates any missing
  cards** (e.g. for an exercise type added after the first enrolment). Existing cards and their
  progress are untouched.

```json
{
  "id": "uuid",
  "contentItemId": "uuid",
  "contentItemType": "DIALOGUE",
  "enrolledAt": "2026-08-15T10:00:00Z",
  "excludedAt": null
}
```

**`DELETE /learning-items/{learningItemId}`** → `204`. Sets `excluded_at` (idempotent). Cards are kept.

**`GET /learning-items?contentItemId={uuid}&contentItemType={type}`** → `200` with the item above,
`404` if not enrolled or excluded. Used while browsing dialogues ("am I studying this?").

**`GET /learning-items/{learningItemId}/cards`** → all cards of the item (one per exercise type
and speaker). Used on the dialogue screen: "As Anna: next review in 3 days".

```json
{
  "cards": [{
    "learningCardId": "uuid",
    "learningItemId": "uuid",
    "exerciseType": "TYPING",
    "variantId": "uuid-speaker",
    "nextReviewAt": "2026-08-23T00:00:00Z",
    "intervalDays": 8,
    "repetitionCount": 5,
    "easeFactor": 2.36
  }]
}
```

### 6.2 Queue (`GET /exercises/queue`)

Due cards (`next_review_at <= now`, item not excluded), ordered by `next_review_at, id`.
Query params (all optional): `exerciseType`, `contentItemType`, `page` (default 0), `size`
(default 20, max 100).

```json
{
  "items": [{
    "learningCardId": "uuid",
    "learningItemId": "uuid",
    "contentItemId": "uuid",
    "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING",
    "variantId": "uuid-speaker",
    "nextReviewAt": "2026-08-15T00:00:00Z",
    "intervalDays": 3,
    "repetitionCount": 4
  }],
  "page": 0,
  "size": 20,
  "totalElements": 12
}
```

### 6.3 Sessions (`/exercises/sessions`)

**`POST /exercises/sessions`** — start or resume.

```json
{ "learningCardId": "uuid" }
```

`201` when a session was created, `200` when the open session was returned. `404` for an unknown
or foreign card, `409` when the card's learning item is excluded.

```json
{
  "sessionId": "uuid",
  "learningCardId": "uuid",
  "learningItemId": "uuid",
  "contentItemId": "uuid",
  "contentItemType": "DIALOGUE",
  "exerciseType": "TYPING",
  "variantId": "uuid-speaker",
  "subItemIds": ["uuid-line-1", "uuid-line-3"],
  "startedAt": "2026-08-15T10:00:00Z",
  "completedAt": null
}
```

`subItemIds` are the lines the user must answer, in dialogue order. The client renders the
dialogue from `GET /dialogues/{id}`, which exposes each line's `id` (see §9), and asks for input on
exactly these lines.

**`POST /exercises/sessions/{sessionId}/attempts`** — evaluate one answer.

```json
{
  "contentSubItemId": "uuid-line-1",
  "usedHint": false,
  "answer": { "type": "TEXT", "text": "Բարեւ ձեզ" }
}
```

`answer` is polymorphic on `type`. Only `TEXT` exists today; a future `AUDIO` answer becomes
`{ "type": "AUDIO", "assetId": "uuid" }` without changing this request. The answer type must suit
the card's exercise type (`TYPING` accepts `TEXT`), otherwise `400`.

```json
{
  "attemptId": "uuid",
  "contentSubItemId": "uuid-line-1",
  "usedHint": false,
  "quality": 5,
  "correct": true,
  "expectedText": null
}
```

- `quality` is after the hint penalty.
- `correct` means the answer matched exactly, independent of the hint.
- `expectedText` is only set when `correct = false`.

Errors:
- `404`: unknown or foreign session.
- `409`: the session is completed, or the sub-item is not part of this session (e.g. the other
  speaker's line).
- `400`: validation failed (missing `answer`, missing `text`, unknown `type`, unsuitable type).

**`GET /exercises/sessions/{sessionId}`** — recovery after app restart.

```json
{
  "session": { "...": "same object as the start response" },
  "attemptedSubItems": [{
    "contentSubItemId": "uuid-line-1",
    "attempts": [
      { "attemptId": "uuid", "answerType": "TEXT", "answerText": "Բարև", "usedHint": false,
        "quality": 2, "correct": false, "evaluatedAt": "2026-08-15T10:01:00Z" }
    ]
  }],
  "pendingSubItemIds": ["uuid-line-3"]
}
```

**`POST /exercises/sessions/{sessionId}/complete`** — seal and schedule. No body.
`409` if any sub-item has no attempt, or if the session is already completed.

```json
{
  "sessionId": "uuid",
  "completedAt": "2026-08-15T10:12:00Z",
  "aggregateScore": 75,
  "cardUpdate": {
    "previousIntervalDays": 6,
    "nextIntervalDays": 15,
    "nextReviewAt": "2026-08-30T10:12:00Z",
    "easeFactor": 2.5
  }
}
```

### 6.4 History (`GET /exercises/history`)

Completed sessions, newest first. Optional params: `from`, `to` (ISO instants, inclusive on
`completed_at`), `exerciseType`, `contentItemType`, `page`, `size`.

```json
{
  "items": [{
    "sessionId": "uuid",
    "learningCardId": "uuid",
    "learningItemId": "uuid",
    "contentItemId": "uuid",
    "contentItemType": "DIALOGUE",
    "exerciseType": "TYPING",
    "variantId": "uuid-speaker",
    "startedAt": "2026-08-15T10:00:00Z",
    "completedAt": "2026-08-15T10:12:00Z",
    "aggregateScore": 75
  }],
  "page": 0,
  "size": 20,
  "totalElements": 47
}
```

---

## 7. Evaluation & SM-2

### 7.1 Typing evaluation

Both texts are normalized before comparison:
1. Unicode NFKC. This maps the Armenian ligature `և` to `եւ`, so `Բարև` equals `Բարեւ`.
2. `toLowerCase(Locale.ROOT)`.
3. Remove punctuation (`\p{P}`). This covers `՞ ՛ ՜ ։ ՝` as well as ASCII.
4. Collapse whitespace and trim.

Exact match → `rawQuality = 5`, `correct = true`. Otherwise normalized Levenshtein similarity:
`> 0.8 → 4`, `> 0.6 → 3`, `> 0.4 → 2`, `> 0.2 → 1`, else `0`. Empty answer → `0`.

Hint penalty: `quality = max(0, rawQuality - 1)` when `usedHint`. The penalty is a constant in
`ExerciseSessionService`.

### 7.2 SM-2 (run once, on complete)

```
subItemMean(i)   = mean of the attempt qualities of sub-item i          (retries lower the mean)
sessionMean      = mean of all subItemMean(i)                           (unrounded)
q                = round(sessionMean)                                   (SM-2 input, 0..5)
aggregateScore   = round(sessionMean / 5 * 100)                         (0..100, e.g. 75)

if q >= 3:
    repetitionCount += 1
    intervalDays = 1 if repetitionCount == 1
                   6 if repetitionCount == 2
                   round(intervalDays * easeFactor) otherwise           (previous ease factor)
    easeFactor   = max(1.3, easeFactor + 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
else:
    repetitionCount = 0
    intervalDays    = 1
    easeFactor      unchanged

nextReviewAt = now + intervalDays days;  updatedAt = now
```

A session with zero sub-items is rejected before SM-2 runs (409). The algorithm itself also
rejects empty input. "Now" comes from an injected `java.time.Clock` so tests are deterministic.

---

## 8. Package Structure

Follows the existing layout: entities and enums in `dao.entities`, repositories in `dao.repository`,
services in domain packages, HTTP in `api.*`. Services return records ("resources") built inside
the transaction. With `open-in-view: false`, mapping lazy entities in a controller would throw
`LazyInitializationException`.

| Package | Contents |
|---|---|
| `dao.entities` | `LearningItem`, `LearningCard`, `ExerciseSession`, `ExerciseAttempt`, enums `ContentItemType`, `ExerciseType`, `AnswerType` |
| `dao.repository` | repositories for the four entities; `DialoguePhraseRepository`, `DialogueSpeakerRepository` (read queries for the resolver) |
| `learning` | `ContentItemResolver`, `ContentItemResolvers` (lookup by type), `ExpectedAnswer`, `LearningItemService`, resource records |
| `exercise` | `ExerciseSessionService`, `ExerciseQueueService`, `ExerciseHistoryService`, `Answer`/`TextAnswer`, resource records |
| `exercise.evaluation` | `AnswerEvaluator`, `AnswerEvaluators`, `Evaluation`, `TypingAnswerEvaluator`, `UnsupportedAnswerTypeException` |
| `exercise.srs` | `Sm2Algorithm`, `Sm2Input`, `Sm2Result` |
| `dialogue` | `DialogueContentItemResolver` |
| `core.exception` | existing `EntityNotFoundException` (404), new `ConflictException` (409) |
| `api.learning` | `LearningItemsApi`, controller, DTOs, mapper |
| `api.exercises` | `ExercisesApi`, controller, DTOs (incl. polymorphic `AnswerRequest`), mapper |

Dependencies point one way: `exercise → learning → dao`. `learning` never imports `exercise`.

---

## 9. Changes to Existing Code

- `GetDialogueResponse.DialoguePhraseResponse` gains `id` (the `dialogue_phrases.id`). This is
  additive. The client needs it to match `subItemIds` to lines. `phrase.id` cannot be used because
  phrases are deduplicated: the same phrase used twice in a dialogue has one id.
- `GlobalControllerAdvice` gains handlers for `ConflictException` (409) and
  `UnsupportedAnswerTypeException` (400).
- A `Clock` bean (`config.ClockConfig`) is added; `BaseIT` replaces it with a fixed clock.

---

## 10. Testing Strategy

Integration tests follow the existing conventions (`DialogueControllerIT`):

- **State is prepared with SQL fixtures; each test calls only the endpoint under test.**
  Fixtures use hard-coded UUIDs and timestamps, and the clock is fixed, so responses are compared
  in full against JSON files with `JsonCompareMode.STRICT`.
- **Mutating calls also assert the resulting database rows.**
- Test names follow `endpoint_condition`, e.g. `startSession_whenSessionInProgress`.
- The existing `insert-dialogue.sql` (two speakers, three lines, with transcriptions) is the
  dialogue for all SRS tests.

| Test | Covers |
|---|---|
| `LearningItemsControllerIT` | enrol new (cards per speaker), re-enrol excluded (progress kept), enrol active, enrol unknown content, validation, exclude, exclude foreign, lookup (found / excluded / not enrolled), cards (found / foreign) |
| `ExerciseSessionControllerIT` | start (new / resume / excluded / foreign), attempt (correct incl. `և` normalization, wrong, hint, other speaker's line, completed session, foreign session, invalid answer), get session, complete (happy path with retries + hint, pending, already completed) |
| `ExerciseQueueControllerIT` | due vs future card, no filters (null parameters), filters, excluded item |
| `ExerciseHistoryControllerIT` | completed sessions, filters |
| `Sm2AlgorithmTest` | interval progression, reset, ease factor unchanged on failure, ease factor floor, averaging, score not a multiple of 20, empty input rejected |
| `TypingAnswerEvaluatorTest` | exact, case, `և`/`եւ`, punctuation incl. `՞`, whitespace, typo, incomplete, mismatch, empty |
| `JpaEntitiesIT` | `toString`/`equals`/`hashCode` of the four new entities |

---

## 11. Error Handling

| Situation | Status | Exception |
|---|---|---|
| Learning item / card / session / content item not found, or owned by another user | 404 | `EntityNotFoundException` |
| Starting a session on an excluded item | 409 | `ConflictException` |
| Sub-item not part of the session | 409 | `ConflictException` |
| Attempt on, or completion of, a completed session | 409 | `ConflictException` |
| Completing with pending sub-items | 409 | `ConflictException` |
| Answer type unsuitable for the exercise type | 400 | `UnsupportedAnswerTypeException` |
| Bean validation / unreadable JSON | 400 | existing `ValidationControllerAdvice` / Spring defaults |

---

## 12. Extension Guide

### 12.1 Spoken answers (`SPEAKING`)

| Change | Where | Breaks clients? |
|---|---|---|
| `ExerciseType.SPEAKING`, `AnswerType.AUDIO` + migration replacing the two named CHECK constraints | `dao.entities`, Flyway | no (new values) |
| Upload endpoint for the recording (reusing `ObjectStorageService` / `medias`) returning an asset id | new `api` endpoint | no (new endpoint) |
| `AudioAnswerRequest { type: "AUDIO", assetId }` subtype, `AudioAnswer` domain record | `api.exercises`, `exercise` | no (new subtype) |
| `answer_media_id` column on `exercise_attempts` + CHECK `AUDIO ⇒ answer_media_id NOT NULL` | Flyway, `ExerciseAttempt` | no |
| `SpeakingAnswerEvaluator implements AnswerEvaluator` | `exercise.evaluation` | no |
| Optional extra feedback, e.g. `recognizedText` on the attempt response | `api.exercises` | no (new nullable field) |
| `DialogueContentItemResolver.supportedExerciseTypes()` returns `TYPING, SPEAKING` | `dialogue` | no |
| Cards for already-enrolled items: one `INSERT … SELECT` in the same migration (re-enrolling also fills gaps) | Flyway | no |

If evaluation becomes slow enough to need asynchronous processing, `quality`/`correct` become
nullable with an evaluation status. That is a migration plus a new optional response field, not a
redesign. Until then evaluation stays synchronous, like the existing multi-second Gemini chat
endpoint.

### 12.2 New content type (e.g. `VOCABULARY`)

Add the enum value + CHECK migration, then implement `ContentItemResolver` for it. No exercise
code changes.

---

## 13. Out of Scope / Follow-ups

- Display data in queue/history items (dialogue title, speaker name). Today the client fetches the
  dialogue separately; adding a `title` field later is additive.
- Daily limit for new cards (enrolling 30 dialogues makes 60 cards due today).
- `phrases.transcription NOT NULL`. Transcription is optional in the save-dialogue request today,
  so making it mandatory is a separate change to validation plus a data migration.
