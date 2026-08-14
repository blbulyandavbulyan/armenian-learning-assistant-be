# Spec Review: Spaced Repetition Exercise Mode

**Reviewer posture**: Cynical Principal Engineer, pre-implementation design review  
**Spec**: [`2026-08-14-spaced-repetition-exercise-mode-design.md`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/docs/superpowers/specs/2026-08-14-spaced-repetition-exercise-mode-design.md)  
**Related**: [GitHub Issue #49](https://github.com/blbulyandavbulyan/armenian-learning-assistant-be/issues/49)

---

## Verdict

The spec has a reasonable skeleton. The session lifecycle is sound, SM-2 is correctly reproduced, the attachment-first-then-reference pattern for audio is the right call, and the crash-recovery model is well-thought-out. Those are the nice things I'll say.

Now here's what's wrong.

---

## 🔴 Severity: Critical — Will Bite You in Production

### 1. `learning_cards` auto-created on `POST /complete` — but sessions require a `learningCardId` to start

This is a **chicken-and-egg deadlock** in the API.

> §3, `learning_cards`: *"Auto-created on the first `POST /exercises/sessions/{id}/complete`"*

> §5.3: `POST /exercises/sessions` request body: `{ "learningCardId": "uuid" }`

So to *start* a session, the client needs a `learningCardId`. But the `learning_card` doesn't exist until the client *completes* a session. How does the very first session for a `(learningItem, exerciseType)` pair begin?

The queue endpoint (`GET /exercises/queue`) returns `LearningCard`s. A brand-new learning item has no cards. So it never appears in the queue. So the user can never start their first exercise. Ever.

**You need one of:**
- Auto-create `LearningCard` at enrolment time (at `POST /learning-items`), with a `next_review_at = now()` so it immediately appears in the queue. This is the cleanest option.
- A separate "start first exercise" flow that takes `learningItemId` + `exerciseType` instead of `learningCardId`. This is ugly and duplicates session-start logic.
- Document that cards are created lazily but then the session-start endpoint must accept `learningItemId` + `exerciseType` as *alternative* input, and create the card inline. This pollutes the API.

Pick one. Right now the spec has a logical impossibility.

---

### 2. No `user_id` on `exercise_sessions`, `exercise_attempts`, or `session_attachments` — authorization is join-chain dependent

To verify that session `X` belongs to the authenticated user, you must: `exercise_sessions` → `learning_cards` → `learning_items` → check `user_id`.

That's a **3-table join for every single authorization check**. Every `POST /evaluate`, every `POST /complete`, every `GET /status`, every `POST /attachments`. Under load, this is death by join. And if you ever refactor the table relationships, you lose your security boundary.

> [!WARNING]
> This is the kind of design that's "correct" in a whiteboard session and becomes a performance and security incident in production.

**Fix**: Denormalize `user_id` onto `exercise_sessions`. Every session-scoped operation becomes a single-table ownership check: `WHERE id = ? AND user_id = ?`. Cheap. Obvious. Impossible to screw up during refactoring.

---

### 3. `content_sub_item_id` is an opaque UUID with no validation path defined

The spec says `content_sub_item_id` is an *"opaque reference to the sub-item (e.g. `dialogue_phrases.id`)"*. But the system needs to:

1. **Validate** that the submitted `content_sub_item_id` actually belongs to the content item referenced by the session's learning item. Otherwise a client can submit random UUIDs, "complete" their session, and game their SM-2 scores.
2. **Know all sub-items** to enforce the "all sub-items must be evaluated before complete" invariant (§4, line 128-129). The status endpoint (§5.3) returns `pendingSubItemIds`.

How does the server know the complete set of sub-items for a given `(content_item_id, content_item_type)`? For `DIALOGUE`, it queries `dialogue_phrases WHERE dialogue_id = ?`. For `VOCABULARY`? For `GRAMMAR_RULE`? The spec says nothing.

This means:
- There's an implicit, undocumented **content resolver** strategy pattern that must exist per `ContentItemType`.
- This resolver must be defined *in the spec*, because it affects the database schema (you need to know which table to query), the module boundaries (does `larm.exercise` depend on `larm.phrase`?), and the API contract (`pendingSubItemIds` must be computable).

**Without this, the completeness check is unimplementable, and the status endpoint's `pendingSubItemIds` field is a lie.**

---

### 4. The evaluate endpoint has no answer-validation contract for SPEAKING exercises

For TYPING, the spec mentions text comparison and quality scoring (`TypingAnswerEvaluator`). For SPEAKING, the spec says... nothing. The evaluate request sends an `attachmentId`. Then what?

- Does the server transcribe the audio and compare it to expected text? With what? Whisper? Gemini? A custom ASR model?
- What determines quality 0-5 for audio? Pronunciation accuracy? Word-level match? Phoneme distance?
- What's the latency budget? Audio transcription + evaluation could take 2-10 seconds. Is the evaluate endpoint synchronous? If so, the client is blocking on an AI inference call.
- What happens when the transcription service is unavailable?

> [!CAUTION]
> Designing an API contract around a feature whose evaluation logic is "TBD" means the contract itself is speculative. The `attachmentId` field on the evaluate request is fine structurally, but the response contract (`quality`, `correct`, `expectedText`) implies deterministic evaluation that doesn't exist yet for audio.

**Minimum viable fix**: Acknowledge in the spec that `SpeakingAnswerEvaluator` is not in scope for Issue #49, and that the `attachmentId` path in the evaluate endpoint will return `501 Not Implemented` until it is. Don't pretend the API is "generic" when half of it is vapor.

---

### 5. The spec's entity model doesn't match the actual data model — the join chain for "get expected answer" is wrong

The spec casually says `content_sub_item_id` references `dialogue_phrases.id` and implies the Armenian text lives on that row. **It doesn't.**

The actual entity chain is:

```
DialoguePhrase (dialogue_phrases)
    → ManyToOne → Phrase (phrases)    ← Armenian text lives HERE: Phrase.phrase
    → ManyToOne → DialogueSpeaker     ← Speaker identity
    → OneToMany → DialoguePhraseTranslation  ← English translation lives HERE
```

See [`DialoguePhrase.java`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/dao/entities/DialoguePhrase.java#L43-L45): the `phrase` field is a `@ManyToOne` to [`Phrase`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/dao/entities/Phrase.java). The Armenian text is `Phrase.phrase`, not a field on `DialoguePhrase`.

This means the `TypingAnswerEvaluator` needs to: resolve `content_sub_item_id` → `DialoguePhrase` → `Phrase` → `phrase` (the text). That's a 2-table join just to get the expected answer. The spec's evaluation response field `expectedText` implies the evaluator already knows this, but the spec never describes this resolution path.

**The spec must document the actual join chain for answer resolution, or this will be implemented incorrectly.**

---

### 6. Existing draft API contracts in the codebase directly conflict with the spec

The spec was written in a vacuum. The codebase **already has** draft API contracts that overlap with and contradict this spec:

| Existing Code | Spec Equivalent | Conflict |
|---|---|---|
| [`LearningPlanApi`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/learning/plan/LearningPlanApi.java): `POST /learning-plan/items` | `POST /learning-items` | **Different URL**, different naming (`learning-plan` vs `learning-items`), different field names (`targetId`/`targetType` vs `contentItemId`/`contentItemType`) |
| [`LearningPlanApi`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/learning/plan/LearningPlanApi.java): `GET /learning-plan` (returns today's plan with `maxItems`) | `GET /exercises/queue` | **Semantically overlapping** — both answer "what should I study today?" but with different models |
| [`ExerciseApi`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseApi.java): `POST /exercises/progress` with polymorphic [`TrackProgressRequest`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/exercises/TrackProgressRequest.java) | `POST /exercises/sessions/{id}/evaluate` + `POST /exercises/sessions/{id}/complete` | **Fundamentally different approach** — existing code uses a single fire-and-forget progress call; spec uses multi-step stateful session |
| [`LearningItemType`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/learning/plan/LearningItemType.java) enum: `DIALOGUE` | `ContentItemType`: `DIALOGUE \| VOCABULARY \| ...` | Different enum name, different package location |
| [`ExerciseType`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseType.java) enum: `TYPING, SPEAKING` | `ExerciseType`: `TYPING \| SPEAKING \| MULTIPLE_CHOICE \| …` | Same values but spec adds more; **different package** |
| [`PhraseTypingAttempt`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/exercises/PhraseTypingAttempt.java): field `dialoguePhraseId` | Spec: `contentSubItemId` | **Different naming**, existing code is dialogue-specific while spec is generic |
| [`PhraseTypingAttempt`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/exercises/PhraseTypingAttempt.java): field `tipUsed` | Spec: `usedHint` | **Different naming** for the same concept |
| [`LearningPlanResponse`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/learning/plan/LearningPlanResponse.java): includes `availableExercisesTypes` per item | Spec: no equivalent — card is per `(item, exerciseType)` | **Different cardinality model** — existing code treats exercise types as a list per item; spec treats each as a separate card |

> [!IMPORTANT]
> The spec must explicitly state: **"The following existing API interfaces will be deleted/replaced: `LearningPlanApi`, `ExerciseApi`, `AddLearningItemRequest`, `TrackProgressRequest`, `DialogueSpeakerRoleplayingTypingRequest`, `PhraseTypingAttempt`, `LearningPlanResponse`, `LearningItemType`."** Otherwise the implementer will try to reconcile two incompatible designs and produce a Frankenstein.

Additionally, the existing [`LearningPlanResponse`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/learning/plan/LearningPlanResponse.java) has an interesting design element the spec lacks: `availableExercisesTypes` per item. This tells the client "you can do TYPING and SPEAKING for this dialogue." The spec has no equivalent — the client must independently know which exercise types apply to which content types. Consider carrying this forward.

---

## 🟠 Severity: High — Design Smell That Will Cause Pain

### 7. Module boundary violation: `larm.exercise` will inevitably depend on `larm.phrase` (and ArchUnit will catch it)

The spec puts everything exercise-related in `larm.exercise`. But the evaluate endpoint for TYPING needs the **expected Armenian text** from `dialogue_phrases` → `phrases`. The status endpoint needs the **complete list of sub-items** from `dialogue_phrases`. The completeness check on `/complete` needs the same.

So `larm.exercise` must query `larm.phrase` / `larm.dao` data. In a Spring Modulith project, this means either:
- **Direct dependency**: `larm.exercise` imports from `larm.phrase` / `larm.dao`. This creates a hard coupling. When you add VOCABULARY, `larm.exercise` depends on even more modules.
- **Exposed service interface**: `larm.phrase` exposes a `ContentResolver` interface that `larm.exercise` consumes.

Critically, the existing codebase has [**ArchUnit enforcement**](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/dao/entities/Dialogue.java) that prevents cross-module `api/` imports. The spec's module structure will need to play by these rules, and the spec doesn't mention this enforcement at all.

**The spec needs to explicitly define the cross-module data access pattern.** A `ContentItemResolver` interface in `larm.exercise` that other modules implement is the right approach, but it must be specified.

---

### 8. The speaker dimension is completely ignored

The existing [`DialogueSpeakerRoleplayingTypingRequest`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/exercises/DialogueSpeakerRoleplayingTypingRequest.java) name tells a story: the exercise is "speaker roleplaying" — the user plays one speaker's role in the dialogue, typing their lines.

But the spec models exercises as "evaluate all sub-items (phrases) in a dialogue." Which phrases? *All of them*? A dialogue has 2+ speakers. The user shouldn't be typing lines for *both* speakers — they should be playing one role.

This means:
- The session needs to know **which speaker** the user is practicing as
- Only that speaker's phrases are "sub-items" for evaluation
- The other speaker's phrases are shown as context/prompts

The spec has zero concept of this. The `content_sub_item_id` model treats all phrases equally. This is a fundamental domain modeling gap that will surface the moment anyone tries to build a UI on top of this API.

**Fix**: Either add `speakerId` to the session creation request (so the server knows which phrases to include in the exercise), or document that the first version evaluates *all* phrases regardless of speaker (and acknowledge this is weird UX).

---

### 9. No index strategy for the queue query

The queue query (`GET /exercises/queue`) is:
```sql
SELECT lc.* FROM learning_cards lc
JOIN learning_items li ON lc.learning_item_id = li.id
WHERE li.user_id = ? AND li.excluded_at IS NULL AND lc.next_review_at <= now()
ORDER BY lc.next_review_at ASC
```

This is the **most frequently executed query** in the entire system (every time the user opens the app). Without a composite index on `learning_items(user_id, excluded_at)` and `learning_cards(learning_item_id, next_review_at)`, this becomes a sequential scan party as the data grows.

The spec defines schema but zero indexing strategy. For a feature that's fundamentally about "show me what's due", this is a significant omission. The Flyway migration must include these indexes.

---

### 10. Attachment lifecycle and garbage collection are undefined

`session_attachments` stores object keys. When does cleanup happen?

- When a session is completed, are attachments retained forever? Audio files are large.
- If a session is abandoned (never completed), do orphaned attachments get cleaned up? By what mechanism? A scheduled job?
- What's the storage quota per user?
- What happens if the client uploads an attachment but never references it in an evaluate call? Orphaned blob.
- What's the maximum file size? The spec says `400 Bad Request` for unpermitted MIME types but nothing about size limits.

This is a **resource leak waiting to happen**. The spec needs at minimum: max file size, retention policy for completed sessions, and a cleanup mechanism for abandoned sessions.

---

### 11. No concurrency control on session state transitions

What happens if two clients (or two browser tabs) hit `POST /evaluate` for the same `content_sub_item_id` simultaneously?

The unique constraint `(session_id, content_sub_item_id)` on `exercise_attempts` will cause a DB-level conflict on one of them. Good. But:

- What about two concurrent `POST /complete` calls? If the completeness check passes for both before either sets `completed_at`, SM-2 runs twice. The `learning_card` gets double-updated.
- What about `POST /evaluate` racing with `POST /complete`? Complete checks "all evaluated", passes. Meanwhile evaluate is inserting a late attempt. Now you have an attempt that was never factored into the SM-2 score.

**Fix**: `POST /complete` must use `SELECT ... FOR UPDATE` on the session row (or optimistic locking with a version column). The spec should specify this.

---

### 12. `aggregateScore` is returned but never defined

The complete response includes `"aggregateScore": 78`. What is this? It's not part of SM-2. It's not the average quality (which would be 0-5, not 78). Is it a percentage? Percentage of what? Quality 5 = 100%, quality 0 = 0%? Is it `(avgQuality / 5) * 100`?

The spec defines the SM-2 algorithm precisely but hand-waves the one number the user actually sees. **Define it.**

---

## 🟡 Severity: Medium — Ambiguities and Missing Details

### 13. `POST /learning-items` idempotency is under-specified for the re-enroll case

> *"If previously excluded, clears `excluded_at` and returns the existing record."*

What HTTP status code? 200? 201? The spec says 201 for new enrollment and "returns the existing record" for re-enroll, but doesn't specify the status code for re-enrollment. In REST semantics, re-enrollment isn't creation (the resource already exists), so it should be 200. But the spec only shows `// Response 201`. This will confuse client developers.

---

### 14. `GET /learning-items/{learningItemId}/card?exerciseType={type}` — awkward resource modeling

This endpoint returns a `LearningCard` nested under a `LearningItem`. But sessions are started by `learningCardId`. So the client workflow is:

1. `GET /exercises/queue` → gets `learningCardId` → starts session. Fine.
2. User browses their dialogues → selects one → wants to start an exercise → needs to... `GET /learning-items/{id}/card?exerciseType=TYPING` to get the `learningCardId`, then `POST /exercises/sessions` with it.

For flow (2), the client needs to already know the `learningItemId` for that dialogue. How? There's no endpoint to look up a learning item by `contentItemId`. The user knows they want to practice dialogue `X` — they don't know the `learningItemId` for it.

**Missing endpoint**: `GET /learning-items?contentItemId={uuid}&contentItemType=DIALOGUE` or add the learning item lookup to the dialogue response.

---

### 15. Evaluate request accepts batch but has no transactional guarantee documented

The evaluate endpoint accepts `"attempts": [...]` as an array. What happens if attempt 1 succeeds but attempt 2 fails validation (e.g., already evaluated)?

- Is it all-or-nothing (transactional)? Then attempt 1 is rolled back.
- Is it partial-success? Then the response needs to indicate which succeeded and which failed.

The spec returns a flat `"results"` array, implying all-or-nothing. But the `409 Conflict` error for duplicate evaluation suggests individual attempt validation. **Specify the atomicity semantics explicitly.**

---

### 16. The spec doesn't mention the `/api/` prefix

Every existing endpoint in the project uses `/api/` prefix ([`DialogueController`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueController.java): `/dialogues` under an API gateway, [`ChatController`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/api/chat/ChatController.java): `/chat`). The spec defines endpoints as `/learning-items`, `/exercises/sessions`, etc.

The [`SecurityConfig`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/config/SecurityConfig.java) gates authentication on specific patterns. If the new endpoints don't match the existing security filter rules, they'll either be unauthenticated (security hole) or blocked entirely. **Verify and document the URL prefix.**

---

### 17. No `created_at`/`updated_at` audit columns on core tables

`learning_cards` has no `created_at`. `exercise_attempts` has `evaluated_at` but no `created_at`. `session_attachments` has `uploaded_at` which is fine. But `learning_cards` — the table you'll be querying most for debugging and analytics — has no timestamp for when it was created or last modified.

When you're debugging why a user's interval jumped from 1 to 60 days, you'll wish you had `updated_at` on `learning_cards`.

---

### 18. The `usedHint` flag is client-asserted and therefore untrusted

The client sends `"usedHint": true/false`. The server applies a quality penalty based on this flag. But the client can always send `false`. There's no server-side mechanism to track whether a hint was actually shown.

This is a fundamental integrity problem for any SRS system — the client can trivially game their scores by never admitting to using hints. If the hint content is served by the server (which it must be, since the server knows the expected text), then the server should track whether a hint was requested via a separate `POST /exercises/sessions/{id}/hint?contentSubItemId={id}` endpoint, and set `usedHint` server-side.

If you don't care about gaming (it's a personal learning app, the user only cheats themselves), **document that design decision explicitly** so nobody "fixes" it later.

---

## 🟢 Severity: Low — Nits and Future-proofing

### 19. `ease_factor` as `REAL` (32-bit float) will accumulate rounding errors

SM-2 ease factor adjustments are small (`± 0.02` to `± 0.1`). After hundreds of reviews, 32-bit float arithmetic will drift. Use `NUMERIC(4,2)` or `DOUBLE PRECISION` instead.

### 20. `content_item_type` and `exercise_type` as `VARCHAR` — use PostgreSQL enums or check constraints

Without a `CHECK` constraint, nothing prevents `content_item_type = 'DLAIOGUE'` (typo) from being inserted. Either use PostgreSQL `CREATE TYPE ... AS ENUM` or add `CHECK (content_item_type IN ('DIALOGUE', 'VOCABULARY', ...))`. The Java enum provides no protection at the database layer.

### 21. History endpoint returns `aggregateScore` but the schema doesn't store it

The `exercise_sessions` table has no `aggregate_score` column. The history endpoint returns it. So either it's computed on the fly from `exercise_attempts` (expensive join + aggregation on every history query), or the spec forgot to add the column. Add the column and compute-once-on-complete.

### 22. `Dialogue` entity has no `user_id` — spec's `learning_items.content_item_id` has no ownership validation

The [`Dialogue`](file:///media/share/ds1/Projects/Personal/IdeaProjects/armenian-learning-assistant/src/main/java/com/blbulyandavbulyan/larm/dao/entities/Dialogue.java) entity has no `user` field. When `POST /learning-items` receives a `contentItemId` of type `DIALOGUE`, there's no way to verify that the dialogue belongs to the authenticated user. A user could enrol someone else's dialogue. The spec doesn't address content ownership validation at enrolment time.

---

## Summary: Top 7 Items to Resolve Before Implementation

| # | Issue | Action |
|---|---|---|
| 1 | **Chicken-and-egg**: can't start first session without a card that doesn't exist yet | Decide when `LearningCard` is created. Recommend: at enrolment time. |
| 2 | **Existing code conflicts**: draft `LearningPlanApi`, `ExerciseApi`, and DTOs contradict the spec | Explicitly list what gets deleted/replaced. |
| 3 | **Entity model mismatch**: `DialoguePhrase` → `Phrase` indirection not accounted for | Document the actual join chain for answer resolution. |
| 4 | **Speaker dimension missing**: exercises should be per-speaker-role, not all phrases | Add `speakerId` to session or acknowledge the gap. |
| 5 | **Content resolver**: how does `larm.exercise` know the sub-items of a content item? | Define `ContentItemResolver` interface and cross-module contract. |
| 6 | **SPEAKING evaluation is vapor** | Either scope it out of Issue #49 explicitly, or define the evaluation pipeline. |
| 7 | **Authorization cost**: 3-join chain for ownership checks | Denormalize `user_id` onto `exercise_sessions`. |

Everything else is fixable during implementation. These seven will cause rework or architectural dead-ends if not addressed in the spec.
