---
base: ac2ebeae0513cc1ee30f9c240aa2f8971448d945
audited-base: ac2ebeae0513cc1ee30f9c240aa2f8971448d945
audited-head: 8031e0b1b4ff7bef3b67e4615c55d663f5b6082b
implementation: 8031e0b1b4ff7bef3b67e4615c55d663f5b6082b
head: 8031e0b1b4ff7bef3b67e4615c55d663f5b6082b
reviewers: 4 read-only Opus subagents (correctness, security, ticket-fit, tests)
harness: claude-code
session: f166b484-514d-42dd-b047-3541829f58e4
fixed-in: HEAD
anchors: review-commit
---

## Fixed

### Repeated early reviews overflow the SM-2 interval and break completion
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/srs/Sm2Algorithm.java:42
- file: src/test/java/com/blbulyandavbulyan/larm/exercise/srs/Sm2AlgorithmTest.java:93
- source: correctness reviewer
- severity: medium
- observation: startSession has no due check, so a card can be completed about 22 times in a row; interval*EF then wraps the int, violating the CHECK and returning 500 forever.
- fix: cap the interval at 3650 days.

### Answer text had no size limit
- file: src/main/java/com/blbulyandavbulyan/larm/api/exercises/TextAnswerRequest.java:12
- file: src/test/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseSessionControllerIT.java:186
- source: security reviewer
- severity: medium
- observation: a multi-MB answer runs NFKC, a regex and O(n*m) Levenshtein, then is stored in an unbounded TEXT column.
- fix: validate text with @Size(max = 1000); an over-long answer is now a 400.

### History user isolation and hint-floor behaviour were untested
- file: src/test/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseHistoryControllerIT.java:18
- file: src/test/java/com/blbulyandavbulyan/larm/api/exercises/ExerciseSessionControllerIT.java:195
- source: tests reviewer
- severity: medium
- observation: no fixture had another user's completed session, so dropping the userId filter in the history query kept tests green; hint on a zero-quality answer (Math.max floor) was also unpinned.
- fix: added another user's completed session to the history fixtures and an empty-answer-with-hint test.

## Ignored

### Speaker with no lines gets a card that can never complete
- file: src/main/java/com/blbulyandavbulyan/larm/learning/LearningItemService.java:96
- source: correctness, ticket-fit reviewers
- severity: medium
- observation: a dialogue saved with an unused speaker gets a due card whose session has no sub-items, so complete always returns 409 and it stays queued.
- why: spec is silent; needs a dialogue-save validation decision. Deferred follow-up; excluding the dialogue is the escape.

### Concurrent first enrolment of the same dialogue returns 500
- file: src/main/java/com/blbulyandavbulyan/larm/learning/LearningItemService.java:58
- source: correctness, ticket-fit reviewers
- severity: medium
- observation: two parallel POST /learning-items both insert; the unique constraint violation is unhandled and returns 500 instead of 200.
- why: accepted limitation in the plan's Self-Review; re-enrolment is safe and a double tap is unlikely.

### Unlimited attempts per session grow the database
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/ExerciseSessionService.java:122
- source: security reviewer
- severity: low
- observation: a script can submit unlimited attempts to an open session with no rate or count cap.
- why: spec allows unlimited retries; rate limiting is a cross-cutting concern outside this feature.

### Wrong answer reveals expectedText, so a retry earns quality 5
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/ExerciseSessionService.java:122
- source: security reviewer
- severity: low
- observation: after a wrong attempt the response carries the correct text, which can be resent for full marks.
- why: retries and showing the answer are explicit spec behaviour (spec 6.3); attempts are averaged per sub-item.

### Attempts and completion still work after the item is excluded
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/ExerciseSessionService.java:167
- source: security, correctness, ticket-fit reviewers
- severity: low
- observation: only startSession rejects an excluded item; an open session can still be answered and completed.
- why: spec only forbids starting; completing an already-started session is harmless.

### Re-enrolment never meets a missing card in tests
- file: src/main/java/com/blbulyandavbulyan/larm/learning/LearningItemService.java:58
- source: tests reviewer
- severity: low
- observation: every re-enrol fixture already has both cards, so skipping createMissingCards would pass.
- why: real gap but low risk; logic is a simple set difference. Noted for a follow-up fixture.

### Ease factor persistence, queue ordering and filters, middle quality bands unpinned
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/ExerciseSessionService.java:157
- source: tests reviewer
- severity: low
- observation: the completion fixture leaves ease at 2.5, one-row queue fixtures hide ordering, and only two similarity bands are asserted.
- why: the algorithm is unit-tested; remaining gaps are boundary polish and left for follow-up.

### Removing punctuation without a space joins adjacent words
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/evaluation/TypingAnswerEvaluator.java:52
- source: correctness reviewer
- severity: low
- observation: a text like "բարև,ընկեր" normalizes to one word, so typing it with a space is marked incorrect.
- why: Armenian punctuation sits inside words (՞), spec chose removal; commas without a following space are rare.

### Cards listed by random speaker UUID and ease factor float noise
- file: src/main/java/com/blbulyandavbulyan/larm/dao/repository/LearningCardRepository.java:23
- source: ticket-fit, correctness reviewers
- severity: info
- observation: GET cards orders by variantId rather than speaker order, and easeFactor can serialize as 2.3600000000000003.
- why: cosmetic; the client can order by the dialogue's speakers and round for display.

### Initial ease factor 2.5 defined in two places
- file: src/main/java/com/blbulyandavbulyan/larm/learning/LearningItemService.java:29
- source: ticket-fit reviewer
- severity: info
- observation: Sm2Algorithm and LearningItemService each keep their own 2.5.
- why: deliberate so learning does not depend on exercise (plan Task 8).

## Assumptions

### Interval cap of 3650 days (ten years)
- file: src/main/java/com/blbulyandavbulyan/larm/exercise/srs/Sm2Algorithm.java:17
- why: the spec names no cap; ten years is far beyond any real use. A smaller cap such as one year would change long-term scheduling.
- alternative: cap at 365 days
- confidence: 0.55

### Typed answer length limit of 1000 characters
- file: src/main/java/com/blbulyandavbulyan/larm/api/exercises/TextAnswerRequest.java:15
- why: dialogue lines are short, so 1000 characters is generous; unknown future long phrases could exceed it.
- alternative: no limit, rely on request body size
- confidence: 0.7

### Plan and spec followed as written, including endpoint shapes
- file: src/main/java/com/blbulyandavbulyan/larm/learning/LearningItemService.java:58
- why: all API shapes, status codes and SM-2 rules were taken verbatim from the plan; I did not question the 200 versus 201 enrol semantics.
- alternative: always return 201 on enrol
- confidence: 0.85
