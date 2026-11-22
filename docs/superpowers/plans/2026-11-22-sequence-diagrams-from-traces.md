# Sequence Diagrams from Traces Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integration tests tagged `@GenerateSequence` (or selected via `GENSEQ_SELECT`) write deterministic `.genseq.puml` + `.genseq.json` sequence diagrams, recorded from real Micrometer/OpenTelemetry traces, so human-review's Sequence tab and C2 view render.

**Architecture:** The application gets tracing once (HTTP server, our components via one aspect, JDBC, Piper `RestClient`, Spring AI chat, a hand-opened embedding observation). Tests export spans to an in-memory exporter; a JUnit 5 extension on `BaseIT` turns each traced test's spans into a call tree (`SpanTree`) and writes it (`GenseqWriter`) under `docs/generated/sequence/`.

**Tech Stack:** Spring Boot 4.0.6, Java 25, Micrometer Observation + Tracing (OpenTelemetry bridge), `opentelemetry-sdk-testing` (`InMemorySpanExporter`), datasource-micrometer, JUnit 5, PlantUML text.

**Spec:** `docs/superpowers/specs/2026-11-22-sequence-diagrams-from-traces-design.md`

## Global Constraints

- Spring Boot 4.0.6, Java 25, Spring AI 2.0.0-M6, Spring Modulith 2.0.6; base package `com.blbulyandavbulyan.larm`.
- `mvn clean compile` runs Checkstyle + PMD; JUnit 5 test classes/methods package-private. Run ITs with `mvn verify`, never `mvn test`.
- `ArchitectureTest`: nothing outside `api` depends on `api`; Swagger annotations only in `api`.
- Nothing exported in production by default (no OTLP endpoint configured).
- Generated files must be byte-identical across runs: no span ids, timestamps, durations, concrete UUIDs; route templates (`/dialogues/{id}`), never concrete URLs; JSON keys sorted.
- Output: `docs/generated/sequence/<TestClass>.java.<scenario-slug>.genseq.puml` and `.genseq.json`; covered by CI's existing `git status --porcelain -- openapi.yaml docs/generated` check.
- Lifelines exactly: `Test`, `Backend`, `DB`, `Piper`, `Gemini`. Components are nested self-calls on `Backend`.
- Header line parsed by the skill (regex `^(?:title|==)\s*\[\[src://(?P<path>[^\s:{\]]+):(?P<line>\d+)(?:\{(?P<tip>[^}]*)\})?\s*(?:Test:\s*)?(?P<title>[^\]]*?)\s*\]\]`): `title [[src://src/test/java/<pkg path>/<Class>.java:<line>{Click to open the test} Test: <Class>: <scenario>]]`.
- Reference sample of the target format: `~/human-review/demo/demo2/assets/genseq/petclinic-test/generated/VisitGridTest.java.serves-the-newest-ten-first.genseq.{puml,json}`.

## Review Focus

- A traced test that **fails** must not write or overwrite its diagram (a red test would commit a broken picture) — Task 6.
- A test making **several HTTP requests** (`getDialogue_cachesResults`) draws them all, in request order, each with its own nested calls — Task 4.
- **Error responses** (4xx from validation, 5xx) still draw the request and `Backend --> Test: <status>` — Task 4.
- **Running the same test twice** produces byte-identical files, including `genseq://` ids and JSON — Task 5 and Task 6.
- **No `GENSEQ_SELECT` set** (plain `mvn verify` in CI) writes diagrams only for tagged tests, nothing else — Task 6.

---

### Task 1: Tracing infrastructure, HTTP server and Piper spans

**Files:**
- Modify: `pom.xml` (tracing bridge + test deps)
- Modify: `src/main/java/com/blbulyandavbulyan/larm/ai/tts/PiperTextToSpeechRestService.java:18-23`
- Modify: `src/test/resources/application-test.yaml`
- Create: `src/test/java/com/blbulyandavbulyan/larm/genseq/TracingTestConfiguration.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/BaseIT.java` (`@Import` the configuration)
- Test: `src/test/java/com/blbulyandavbulyan/larm/genseq/TracingIT.java`

**Interfaces:**
- Produces: bean `InMemorySpanExporter spanExporter` in every `BaseIT` context; static helper `List<SpanData> TracingTestConfiguration.finishedSpans(ApplicationContext ctx)` which force-flushes the `SdkTracerProvider` (`forceFlush().join(5, SECONDS)`) and returns `spanExporter.getFinishedSpanItems()`; `void TracingTestConfiguration.reset(ApplicationContext ctx)`.

- [ ] **Step 1: Add dependencies.** Boot 4's Micrometer-tracing OpenTelemetry bridge (resolve the right artifact for 4.0.6 — `spring-boot-starter-opentelemetry` or `spring-boot-micrometer-tracing-opentelemetry` + `micrometer-tracing-bridge-otel`; check with `mvn dependency:tree`), and test-scope `io.opentelemetry:opentelemetry-sdk-testing` (version from the OpenTelemetry BOM Boot manages). No OTLP endpoint property anywhere.
- [ ] **Step 2: Test profile** `application-test.yaml`: `management.tracing.sampling.probability: 1.0`.
- [ ] **Step 3: Write the failing test** `TracingIT extends BaseIT`, `@WithMockDatabaseUser`:

```java
@Test
void saveDialogue_producesServerSpanWithRouteAndPiperClientSpanInSameTrace() throws Exception {
    // stub Piper + embedding exactly as DialogueControllerIT.saveDialogue does, POST the same fixture
    List<SpanData> spans = TracingTestConfiguration.finishedSpans(context);
    SpanData server = spans.stream().filter(s -> s.getKind() == SpanKind.SERVER).findFirst().orElseThrow();
    assertThat(server.getAttributes().get(AttributeKey.stringKey("http.route"))).isEqualTo("/dialogues"); // the save route template
    assertThat(spans).anySatisfy(s -> {
        assertThat(s.getKind()).isEqualTo(SpanKind.CLIENT);
        assertThat(s.getTraceId()).isEqualTo(server.getTraceId());
    });
}
```

(Use the real save route from `DialogueApi`; the Micrometer key may be `uri` rather than `http.route` — assert whichever the bridge emits, and record it as a constant for Task 4.)
- [ ] **Step 4: Run** `mvn verify -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=TracingIT` — Expected: FAIL (no exporter bean / no spans).
- [ ] **Step 5: Implement** `TracingTestConfiguration` (`@TestConfiguration`, exposes `InMemorySpanExporter` as the `SpanExporter` Boot wires into the tracer provider) and import it in `BaseIT`. Change `PiperTextToSpeechRestService` constructor to take `RestClient.Builder restClientBuilder` and call `restClientBuilder.baseUrl(...).build()`. If MockMvc does not produce a SERVER span, add `ServerHttpObservationFilter` to MockMvc via `@AutoConfigureMockMvc` filters (it should already be registered as a filter bean).
- [ ] **Step 6: Run** the same command — Expected: PASS. Then `mvn verify` — all existing ITs still pass.
- [ ] **Step 7: Commit** `feat(tracing): OpenTelemetry tracing, Piper via RestClient.Builder, in-memory spans in ITs`.

### Task 2: JDBC spans

**Files:**
- Modify: `pom.xml`
- Test: `src/test/java/com/blbulyandavbulyan/larm/genseq/TracingIT.java`

**Interfaces:**
- Produces: one span per executed SQL statement, child of the current span, carrying the parameterised SQL text in an attribute; record the span name and attribute key as constants `JDBC_SPAN_NAME`, `JDBC_SQL_KEY` in `genseq/SpanNames.java` (create it here) for Task 4.

- [ ] **Step 1: Failing test** `saveDialogue_producesSqlSpansInsideTheRequest`: after the same save request, assert at least one span with `JDBC_SQL_KEY` whose value starts with `insert into` (case-insensitive), with the server span's trace id, and that no SQL value contains a literal UUID (regex `[0-9a-f]{8}-[0-9a-f]{4}-`).
- [ ] **Step 2: Run** the IT — Expected: FAIL.
- [ ] **Step 3: Implement.** Add `net.ttddyy.observation:datasource-micrometer-spring-boot` in a version supporting Boot 4 (check its release notes). Configure `jdbc.datasource-proxy.include-parameter-values: false` (or the library's equivalent) and only query spans (`jdbc.includes: QUERY`). If no Boot-4-compatible version exists: a Hibernate `StatementInspector` (`hibernate.session_factory.statement_inspector`) in `com.blbulyandavbulyan.larm.observability` that starts and stops an `Observation` named `jdbc.query` with high-cardinality key `jdbc.query` = SQL, around nothing (zero duration; order is what matters).
- [ ] **Step 4: Run** — Expected: PASS; `mvn verify` green.
- [ ] **Step 5: Commit** `feat(tracing): a span per SQL statement`.

### Task 3: Component and embedding observations

**Files:**
- Create: `src/main/java/com/blbulyandavbulyan/larm/observability/ComponentObservationAspect.java`
- Modify: `pom.xml` (`spring-boot-starter-aspectj` / AOP if not present)
- Modify: `src/main/java/com/blbulyandavbulyan/larm/ai/embedding/DialogueVectorizationService.java:49`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dialogue/DialogueSearchService.java:23`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/genseq/SpanNames.java`
- Test: `src/test/java/com/blbulyandavbulyan/larm/genseq/TracingIT.java`

**Interfaces:**
- Produces: spans named `larm.component` with low-cardinality keys `code.namespace` (simple name of the target class; for Spring Data repositories the first proxied interface under the base package, e.g. `DialogueRepository`) and `code.function` (method name); spans named `gen_ai.embedding`. Constants `COMPONENT_SPAN`, `EMBEDDING_SPAN`, `NAMESPACE_KEY`, `FUNCTION_KEY` in `SpanNames`.

- [ ] **Step 1: Failing tests:**
  - `saveDialogue_producesComponentSpans`: spans contain `code.namespace=DialogueOrchestrator, code.function=saveDialogue` and a `PhraseProcessor` span nested under it (parent chain), and a repository span (`code.namespace` ending in `Repository`).
  - `saveDialogue_producesEmbeddingSpan`: a span named `gen_ai.embedding` in the request's trace.
  - `componentSpans_excludeMappersAndConfiguration`: no `code.namespace` ending in `Mapper`, none from `..config..`/`..security..` classes.
- [ ] **Step 2: Run** — Expected: FAIL.
- [ ] **Step 3: Implement** `ComponentObservationAspect` (`@Aspect @Component`, takes `ObservationRegistry`), one `@Around` advice: `within(com.blbulyandavbulyan.larm..*)` and (`@within(Service)` or `@within(Component)` or `@within(RestController)` or execution of a `com.blbulyandavbulyan.larm..*Repository` method), excluding `..config..`, `..security..`, `..observability..`, `com.blbulyandavbulyan.larm.api..*Mapper`. Wrap with `Observation.createNotStarted("larm.component", registry).lowCardinalityKeyValue(...).observeChecked(pjp::proceed)` (rethrow as-is). Must not import anything from `..api..`. Around both `embeddingModel.embed(...)` calls, `Observation.createNotStarted("gen_ai.embedding", registry).observe(() -> ...)` (inject `ObservationRegistry`).
- [ ] **Step 4: Run** TracingIT, then `mvn test -Dtest=ArchitectureTest` and `mvn verify` — Expected: PASS.
- [ ] **Step 5: Commit** `feat(tracing): observe our components and embedding calls`.

### Task 4: `SpanTree` — spans to an ordered call tree

**Files:**
- Create: `src/test/java/com/blbulyandavbulyan/larm/genseq/SpanTree.java`, `Call.java`
- Test: `src/test/java/com/blbulyandavbulyan/larm/genseq/SpanTreeTest.java`

**Interfaces:**
- Consumes: `SpanNames` constants (Tasks 1–3).
- Produces: `record Call(Kind kind, String label, String detail, Map<String,String> attributes, List<Call> children)` with `enum Kind { REQUEST, COMPONENT, SQL, PIPER, GEMINI_CHAT, GEMINI_EMBED }`; `static List<Call> SpanTree.requests(List<SpanData> spans)` — one `REQUEST` per HTTP server span, in start-time order.

Rules (the algorithm the tests pin):
- Roots are SERVER spans; everything without a SERVER ancestor is dropped.
- Children ordered by start time.
- `REQUEST.label` = the controller handler method (`code.function` of the first `larm.component` child whose namespace ends in `Controller`, that span itself folded away), `detail` = `<VERB> <route template>`; `attributes` carry `status` (`http.response.status_code` / `status`).
- `larm.component` → `COMPONENT`, label `<namespace>.<function>`.
- JDBC → `SQL`, label = SQL text; consecutive identical `SQL` siblings collapse into one with `attributes.count`.
- CLIENT span → `PIPER`, label `<VERB> <path>`.
- Spring AI chat: outermost chat-client/chat-model span → `GEMINI_CHAT`, its descendants dropped. `gen_ai.embedding` → `GEMINI_EMBED`.
- Dropped and their children re-parented upward: Spring Security (`spring.security.*`), Spring Modulith module spans, any other unknown span.

- [ ] **Step 1: Failing tests** (build `SpanData` with `io.opentelemetry.sdk.testing.trace.TestSpanData`):
  - `dropsSpansOutsideAnyRequest`
  - `twoRequestsInOneTestAreTwoRootsInOrder` (Review Focus)
  - `foldsControllerSpanIntoRequestLabel` — label `saveDialogue`, detail `POST /dialogues`
  - `errorResponseKeepsStatus` — a 400 server span yields `attributes.status == "400"` (Review Focus)
  - `collapsesConsecutiveIdenticalSql` — 3 identical → one with `count == "3"`; A,B,A stays three
  - `chatClientDescendantsAreOneGeminiCall`
  - `unknownSpansAreReparentedNotLost` — a component under a security span ends up directly under the request
- [ ] **Step 2: Run** `mvn test -Dtest=SpanTreeTest` — Expected: FAIL (class missing).
- [ ] **Step 3: Implement** `SpanTree.requests`.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `test(genseq): build call trees from spans`.

### Task 5: `GenseqWriter` — PlantUML and JSON

**Files:**
- Create: `src/test/java/com/blbulyandavbulyan/larm/genseq/GenseqWriter.java`, `SourceLines.java`
- Test: `src/test/java/com/blbulyandavbulyan/larm/genseq/GenseqWriterTest.java`

**Interfaces:**
- Consumes: `Call` (Task 4).
- Produces: `record Diagram(String puml, String json)`; `static Diagram GenseqWriter.render(TestRef test, List<Call> requests)` with `record TestRef(String testClassPath, int line, String className, String scenario, String footer)`; `static String GenseqWriter.slug(String methodName)`; `static OptionalInt SourceLines.methodLine(String simpleClassName, String method)` (scans `src/main/java/**/<Class>.java` for the first line matching `\b<method>\s*\(` that is a declaration).

Output shape (match the reference sample line for line):
- `@startuml`, `' ⚠️  GENERATED FILE — DO NOT EDIT. Every edit is lost on the next run.`, `hide footbox`, the four `skinparam` lines of the sample, the `title` handle (Global Constraints), `footer <footer>`, `participant` lines for `Test`, `Backend` and then only the lifelines used, in order `DB`, `Piper`, `Gemini`.
- REQUEST: `Test -> Backend: [[genseq://<id>{Click for the request as it was sent} <label>\n<detail> ⊕]]`, `activate Backend`, children, `Backend --> Test: <status>`, `deactivate Backend`.
- COMPONENT: `Backend -> Backend: [[src://<repo path>:<line>{Click to open <label>} <label> ↗]]` (plain `<label>` when `SourceLines` finds nothing), then `activate Backend` / children / `deactivate Backend` only if it has children.
- SQL: `Backend -> DB: [[genseq://<id>{Click for the statement behind this call} <sql truncated to 120 chars + " …"> ⊕]]`, suffix ` ×N` when collapsed.
- PIPER: `Backend -> Piper: <label>`; GEMINI_CHAT: `Backend -> Gemini: chat`; GEMINI_EMBED: `Backend -> Gemini: embed`.
- `id` = first 7 chars of base-36 of SHA-256 over `<kind>|<label>|<detail>|<index of this call in pre-order>`.
- JSON: `{"version": 3, "details": {<id>: {"title": ..., "steps": [{"label": ..., "text": ...}]}}}` with keys sorted (`ObjectMapper` with `ORDER_MAP_ENTRIES_BY_KEYS` + sorted properties), REQUEST steps `[{"label":"request","text":"<detail>"}]` (request bodies are left out for now — spec deviation, keeps the files deterministic without masking), SQL steps `[{"label":"","text":<full sql>}]`.
- `slug("saveDialogue_withEmptyTranslations")` → `save-dialogue-with-empty-translations`; scenario = slug with spaces.

- [ ] **Step 1: Failing tests:**
  - `headerMatchesTheSkillsHandleRegex` — the regex from Global Constraints matches the title line, `path` group equals `testClassPath`.
  - `requestArrowCarriesHandlerAndRoute`, `componentLinksToItsSourceLine` (use `DialogueOrchestrator.saveDialogue`, expect `src://src/main/java/com/blbulyandavbulyan/larm/core/DialogueOrchestrator.java:<n>`), `onlyUsedLifelinesAreDeclared`, `collapsedSqlShowsCount`.
  - `renderingTwiceIsByteIdentical` (Review Focus) and `idsDependOnContentNotOnRunOrder`.
  - `jsonKeysAreSorted`, `slugOfCamelCaseAndUnderscores`.
- [ ] **Step 2: Run** `mvn test -Dtest=GenseqWriterTest` — Expected: FAIL.
- [ ] **Step 3: Implement** `GenseqWriter`, `SourceLines`.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `test(genseq): render call trees as genseq PlantUML and JSON`.

### Task 6: `@GenerateSequence`, the extension, first diagrams

**Files:**
- Create: `src/test/java/com/blbulyandavbulyan/larm/genseq/GenerateSequence.java`, `SequenceDiagramExtension.java`
- Modify: `src/test/java/com/blbulyandavbulyan/larm/BaseIT.java` (`@ExtendWith(SequenceDiagramExtension.class)`)
- Modify: `ChatControllerIT.dialogueChat`, `DialogueControllerIT.saveDialogue`, `DialogueControllerIT.getDialogue`, `AssetsControllerIT.getAsset` (add `@GenerateSequence`)
- Create (generated, committed): `docs/generated/sequence/*.genseq.{puml,json}`
- Test: `src/test/java/com/blbulyandavbulyan/larm/genseq/SequenceDiagramExtensionTest.java`

**Interfaces:**
- Consumes: `TracingTestConfiguration.finishedSpans/reset` (Task 1), `SpanTree.requests` (Task 4), `GenseqWriter.render/slug` (Task 5).
- Produces: `@GenerateSequence` (`@Target({METHOD, TYPE}) @Retention(RUNTIME) @Tag("genseq")`); `SequenceDiagramExtension implements BeforeEachCallback, AfterEachCallback`; `static boolean SequenceDiagramExtension.isSelected(Class<?> testClass, String method, boolean annotated, String genseqSelect)`; output dir constant `docs/generated/sequence`.

Behaviour:
- Selected if annotated (method or class) or `<SimpleClassName>#<method>` is in `GENSEQ_SELECT` (system property first, then env var; comma-separated, entries trimmed; a fully qualified class name also matches).
- `beforeEach`: if selected, `reset`. `afterEach`: if selected **and** `context.getExecutionException().isEmpty()`, read spans, render, write both files (UTF-8, `\n` line endings, trailing newline); if the test failed, write nothing (Review Focus). Zero requests → write nothing.
- `TestRef.line` = line of the test method declaration in its source; `footer` = `traced because it is tagged @GenerateSequence` or `traced because human-review selected it — generated from real traces of integration test runs, do not edit ❗`.

- [ ] **Step 1: Failing unit tests** for `isSelected`: `annotatedIsSelected`, `listedSimpleNameIsSelected`, `listedFqcnIsSelected`, `notListedNotAnnotatedIsNotSelected`, `nullSelectSelectsOnlyAnnotated` (Review Focus).
- [ ] **Step 2: Run** `mvn test -Dtest=SequenceDiagramExtensionTest` — Expected: FAIL.
- [ ] **Step 3: Implement** annotation and extension; register on `BaseIT`; tag the four tests.
- [ ] **Step 4: Run** `mvn verify` — Expected: green; four `.puml` + four `.json` under `docs/generated/sequence/`. Open one: `Test -> Backend`, nested `DialogueOrchestrator.saveDialogue`, `Backend -> DB`, `Backend -> Piper`, `Backend -> Gemini: embed`. Render check: `plantuml -tsvg docs/generated/sequence/*.puml` exits 0.
- [ ] **Step 5: Determinism.** Run `mvn verify` again; `git status --porcelain docs/generated` shows no change after committing the first run's output (Review Focus).
- [ ] **Step 6: Failure path.** Temporarily break an assertion in `getAsset`, run that IT, confirm its diagram file is untouched (`git diff --quiet docs/generated/sequence`), revert.
- [ ] **Step 7: Commit** `feat(genseq): sequence diagrams for tagged ITs` (code + generated files).

### Task 7: human-review configuration

**Files:**
- Modify: `human-review.json`

- [ ] **Step 1: Add** `steps.sequence` and `c2` exactly as the spec's *human-review.json* section, checking every key against `~/human-review/skills/human-review/human-review.example.json` (`select.suites.<name>.files/item/join/max`, `commands` with `{"run","when"}`, `trace: false`, `c2.containers` with `kind`/`drop`). The selected-tests command passes `-DGENSEQ_SELECT={tests.java}`; Failsafe must forward it — add `<systemPropertyVariables><GENSEQ_SELECT>${GENSEQ_SELECT}</GENSEQ_SELECT></systemPropertyVariables>` to the failsafe config if needed, or rely on the env var by prefixing the command with `GENSEQ_SELECT={tests.java}`.
- [ ] **Step 2: Verify** with the skill: `~/human-review/skills/human-review/scripts/run-steps.py --base origin/main --only sequence` (then `--only c2`) — Expected: `sequence ran` and `c2 ran`, diagrams listed for the four tagged tests.
- [ ] **Step 3: Commit** `chore(human-review): configure sequence and container view steps`.
