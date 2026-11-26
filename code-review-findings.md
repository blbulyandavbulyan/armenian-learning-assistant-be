# Code review findings: `sequence-diagrams` vs `main`

- Reviewed: 2026-11-26, branch `sequence-diagrams` at `dbd2ac4`, diff `main...HEAD` (54 files).
- Method: max-effort recall review: 5 finder angles, one verification vote per candidate, then a gap sweep.
  No CLAUDE.md applies to this repo.
- Result: 15 findings, 3 important and 12 nits. Every one is introduced by this branch; none was already in `main`.
- Tick a box once the finding is fixed or deliberately dismissed.

## Important

- [ ] **1. Request arrows lose their label when the route has a path variable.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/GenseqWriter.java:117`
  - Problem: `safe()` keeps `{` and `}` in link labels. The Debian PlantUML 1.2020.02 that the README installs
    (human-review runs `plantuml` from PATH) matches the `{tooltip}` up to the *last* `}`. So in `request()`
    (line 156) the tooltip swallows `getDialogue … GET /dialogues/{id}`. SQL labels containing `}` break the
    same way.
  - Failure: rendering the committed `docs/generated/sequence/DialogueControllerIT.java.get-dialogue.genseq.puml`
    or `AssetsControllerIT.java.get-asset.genseq.puml` gives a Test→Backend arrow that shows only `⊕`. Its
    tooltip is `Click for the request as it was sent} getDialogue GET /dialogues/{id`. The SVGs under
    `.human-review/assets/diagrams` show the same. `save-dialogue` has no braces and renders fine.
  - Fix: encode braces in label text as `<U+007B>`/`<U+007D>`, which renders correctly on 1.2020.02 (`~{` does
    not). Replace the text-only `routeTemplateBracesAreEmittedVerbatim` test with one that renders the diagram.

- [ ] **2. A passing IT is reported as failed when its declaration can't be found.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/SequenceDiagramExtension.java:74`
  - Problem: `afterEach` builds the source path from `getRequiredTestClass().getName()`. `declarationLine`
    (lines 104-117) then throws in three cases:
    - a `@Nested` class: the path becomes `OuterIT$Inner.java`, giving `NoSuchFileException`;
    - a test method inherited from a superclass;
    - `@Test void x()` on one line, because the anchored regex rejects leading annotations.
  - Failure: put `@Test @GenerateSequence void x()` inside a `@Nested` class of `DialogueControllerIT`. The body
    passes, afterEach throws, JUnit marks the test FAILED, and `mvn verify` and CI break.
  - Fix: resolve the top-level class of `method.getDeclaringClass()` and allow leading annotations. Never throw
    from afterEach: fall back to line 1, or skip the diagram and log a warning.

- [ ] **3. The stale-files gate never catches an orphaned sequence diagram.**
  `.github/workflows/maven.yml:48`
  - Problem: `git status --porcelain -- openapi.yaml docs/generated` reports only the files that `mvn verify`
    rewrote or created, and nothing ever deletes files in `docs/generated/sequence`. Diagrams of renamed,
    deleted or untagged tests therefore go stale silently. So do GENSEQ_SELECT ("human-review selected it")
    diagrams committed as the README instructs. This contradicts the README, which says CI fails if a
    committed copy is stale.
  - Failure: rename `DialogueControllerIT.getDialogue` and commit the new diagram. The old
    `get-dialogue.genseq.puml/json` stays committed with the old flow, CI is green, and human-review keeps
    diffing a stale flow.
  - Fix: delete `docs/generated/sequence/*.genseq.*` in CI before `mvn verify`, so a missing regeneration shows up
    as a deletion. Write GENSEQ_SELECT output outside the committed directory.

## Nits

- [ ] **4. On PRs, the stale-files gate can fail in a way its own advice cannot fix.**
  `.github/workflows/maven.yml:48`
  - Problem: on pull requests, `actions/checkout` checks out the merge commit `refs/pull/N/merge`. The
    diagrams' `src://…:<line>` numbers are recomputed from main classes on every run, so a diagram added on
    the branch stops matching once `main` shifts a traced method.
  - Failure: the branch adds a tagged IT that links `PhraseProcessor.java:25`, and meanwhile `main` inserts a line
    above `process`. PR CI writes `:26` and exits 1, while `mvn verify` on the branch writes `:25` and shows no
    diff.
  - Fix: keep volatile line numbers out of committed files (for example, resolve links by method name at render
    time). Otherwise make the error message say to merge `main` and regenerate, and require up-to-date branches.

- [ ] **5. Every production embedding call is recorded twice.**
  `src/main/java/com/blbulyandavbulyan/larm/ai/embedding/DialogueVectorizationService.java:52` (also
  `src/main/java/com/blbulyandavbulyan/larm/dialogue/DialogueSearchService.java:26`)
  - Problem: `GoogleGenAiTextEmbeddingModel.call()` already wraps itself in Spring AI's embedding observation,
    which uses the app's registry. The manual `gen_ai.embedding` wrappers exist only because the ITs mock
    `EmbeddingModel`.
  - Failure: one production POST /dialogues produces `gen_ai.embedding` with Spring AI's `gen_ai.client.operation`
    span nested inside it, and a timer for each.
  - Fix: drop the manual observations and make SpanTree recognise Spring AI's embedding span
    (`gen_ai.operation.name=embedding`), or wrap only the test mock.

- [ ] **6. Any unrecognised CLIENT span is drawn as a Piper call, and its children are dropped.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/SpanTree.java:125`
  - Problem: datasource-micrometer's connection and result-set spans are also CLIENT spans, and connection spans
    are the parents of query spans. Diagrams only come out right because the main `application.yaml` sets
    `jdbc.includes: QUERY`. That setting also removes connection and fetch spans in production. Any other
    outbound HTTP client would likewise be labelled Piper.
  - Failure: with `jdbc.includes: CONNECTION,QUERY`, every diagram loses its SQL and gains an empty
    `Backend -> Piper:` arrow instead.
  - Fix: identify Piper by its base URL or client name, and recurse into unclassified CLIENT spans. Move
    `jdbc.includes` to `application-test.yaml` if only the diagrams need it.

- [ ] **7. The TracingIT "Piper client span" test passes with no Piper span at all.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/TracingIT.java:36`
  - Problem: every JDBC query span is a CLIENT span (`QueryContext` is a `SenderContext`), so it satisfies
    `anySatisfy(kind == CLIENT && same trace)`.
  - Failure: revert `PiperTextToSpeechRestService` to `RestClient.builder()`, which produces no HTTP span. The
    test stays green.
  - Fix: require `http.url` to point at the WireMock base URL, or exclude spans that carry `jdbc.query[...]`.

- [ ] **8. Fixture HTTP calls show up in a test's diagram.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/SequenceDiagramExtension.java:57`
  - Problem: spans are reset in beforeEach (here and in BaseIT's superclass `@BeforeEach`) and collected in
    afterEach. MockMvc calls made in a subclass `@BeforeEach` or in any `@AfterEach` are drawn as if the test
    made them.
  - Failure: a `@BeforeEach` that seeds data via `POST /dialogues` puts the whole save flow (6 Piper calls and the
    inserts) in front of the GET in `get-dialogue.genseq.puml`.
  - Fix: reset in `BeforeTestExecutionCallback`, capture the spans in `AfterTestExecutionCallback` (keep them in
    the ExtensionContext store), and write in afterEach only if the test passed.

- [ ] **9. Collapsed SQL can show the wrong `×N` count.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/SpanTree.java:179`
  - Problem: `request()` folds the controller's already-collapsed children into the server-level list and
    collapses it again. `run` restarts at 1 and drops the existing count.
  - Failure: `server → [FooController{select a ×3}, select a]` renders `select a ×2` instead of `×4`.
  - Fix: start `run` from `last.attributes().get("count")` (default 1) and add the incoming call's count.

- [ ] **10. `src://` links can point at a call site, or be missing.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/SourceLines.java:68`
  - Problem: `isDeclaration` cuts the line at `line.indexOf(method)`, the first substring match, not where the
    regex matched. It then accepts any text before the name that has no `=` or `(` and does not end in `.`.
    - These count as declarations: `case 1 -> get(k);`, `yield get(k);`, `: get(y);`, and
      `String widget = get(key);` (indexOf finds `get` inside `widget`).
    - This real declaration is rejected: `@Deprecated(since = "1") public String get(`.
  - Failure: a traced class has `case 1 -> get(k);` on line 5 and `public String get(String k)` on line 9.
    `methodLine` returns 5.
  - Fix: take the text before the name from `call.matcher(line).start()`, and require a type or modifier token
    right before the name. Reject `->`, `yield` and lines that start with an operator.

- [ ] **11. Writing a diagram can fail a passing test (surrogate pair split).**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/GenseqWriter.java:122`
  - Problem: `truncate()` uses `substring(0, 120)`, which can split a surrogate pair. `Files.writeString(..., UTF_8)`
    in `SequenceDiagramExtension.write` then throws `UnmappableCharacterException` out of afterEach.
  - Failure: a traced statement longer than 120 chars has an emoji starting at index 119. Both writes throw and
    the tagged IT is marked failed.
  - Fix: cut on a code-point boundary, for example step back one char when
    `Character.isHighSurrogate(line.charAt(119))`.

- [ ] **12. A diagram can be written from an incomplete span list with no warning.**
  `src/test/java/com/blbulyandavbulyan/larm/genseq/TracingTestConfiguration.java:36`
  - Problem: `forceFlush().join(5, SECONDS)` discards the returned `CompletableResultCode`. A timed-out or failed
    BatchSpanProcessor export leaves `finishedSpans()` incomplete.
  - Failure: on a loaded CI runner, spans are missing, the regenerated `.puml` differs from the committed one, and
    the stale check fails with a confusing diff.
  - Fix: check `isSuccess()` after `join` and fail or skip writing, or use a SimpleSpanProcessor for the
    in-memory exporter in tests.

- [ ] **13. Every component span is titled `larm.component` in tracing backends.**
  `src/main/java/com/blbulyandavbulyan/larm/observability/ComponentObservationAspect.java:43`
  - Problem: no `contextualName` is set, so Micrometer's TracingObservationHandler falls back to the
    observation name.
  - Failure: with OTLP export enabled, a POST /dialogues trace shows about 30 spans that are all named
    `larm.component`. Searching for `DialogueSavingService.saveDialogue` by span name finds nothing.
  - Fix: add `.contextualName(namespace + "." + function)`, and have SpanTree match on the `code.*` attributes
    instead of the span name.

- [ ] **14. The auto-configured `RestClient.Builder` changes how Piper's HTTP client behaves.**
  `src/main/java/com/blbulyandavbulyan/larm/ai/tts/PiperTextToSpeechRestService.java:21`
  - Problem: both before and after, the JDK HTTP client is used. Boot's default settings now follow redirects
    (`Redirect.NORMAL`, which turns a POST into a GET on 301/302). Before, `RestClient.builder()` used
    `HttpClient.newHttpClient()`, which never follows them. Global `spring.http.client.*` settings and any
    `RestClientCustomizer` beans now apply as well.
  - Failure: Piper sits behind a gateway that answers 302. The client now sends a GET with no body and gets a
    405, or gets HTML that is stored as a `.wav`.
  - Fix: if only tracing is wanted, use `RestClient.builder().observationRegistry(registry)`, or set the redirect
    policy explicitly on the injected builder.

- [ ] **15. The blanket pointcut now CGLIB-proxies every matched bean.**
  `src/main/java/com/blbulyandavbulyan/larm/observability/ComponentObservationAspect.java:32`
  - Problem: `AssetsController`, `AssetService`, `PhraseMediaService` and `PhraseProcessor` are proxied for the
    first time. Every future component is now constrained:
    - a `final` class or record annotated `@Service`/`@Component` fails at startup;
    - a `public final` method runs on the proxy instance, whose fields are null.
  - Failure: `@Service public final class Foo` fails with `AopConfigException`. A `public final String id()
    { return dep.x(); }` method throws an NPE when called through the proxy.
  - Fix: document the constraint and enforce it with an ArchUnit rule (or a startup check) that rejects final
    classes and public final methods on beans the aspect matches.

## Checked and not reported

- `@ManyToOne(optional = false)` makes `title` an INNER JOIN. Refuted: `title_phrase_id` is `NOT NULL` with an
  immediate FK, so no rows can be lost. Hibernate's persist-time nullability check stays off because Bean
  Validation is present.
- A missing `it.failIfNoSpecifiedTests` in `human-review.json`. Refuted: failsafe 3.5.6 only fails when no
  selected class exists at all.
- JDK-proxy naming (first `larm` interface), and Modulith observability excluded in tests. Both refuted: with
  this config the first case cannot happen, and the second has no concrete incompatibility.
- The Pitest guard is missing from `OpenApiSpecIT` and `DataModelDiagramsIT`. Not reported: the `dao`, `api` and
  `config` packages are excluded from mutation, so mutants cannot change those files.
- Diagram file-name collisions (same simple class name, or method names that produce the same slug). Cut by the
  15-finding cap: low confidence, and not triggered today.
