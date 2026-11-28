# Sequence diagrams from real traces of integration tests

## Intent

The human-review page (`/human-review`) has a **Sequence** tab: for each traced test, the
sequence diagram its run actually produced, beside the test, with a diff against the base
branch. The Structure tab's C2 container view is projected from the same diagrams. Today
both are skipped on this repository (`sequence.commands not configured`, `no *.genseq.puml`),
because the skill does not ship a generator — the project supplies one.

This change makes the backend produce those diagrams from **real tracing**: Micrometer
Observation + OpenTelemetry, configured once in the application, recorded in-process during
the integration tests, and turned into `.genseq.puml` files by a JUnit extension.

A reviewer should see, per flow: HTTP request in → our own components (controller,
orchestrator, services, repositories) → SQL → Piper → Gemini → response, and what a PR
changed in that flow.

## Goals

- Application-wide tracing configured once: HTTP server, our components, JDBC, Piper
  (`RestClient`), Spring AI chat calls. Usable later for production tracing by setting an
  OTLP endpoint; nothing exported by default.
- Test-time capture with an in-memory span exporter (no Docker, no Tempo, no Grafana).
- A `@GenerateSequence` test annotation; a JUnit 5 extension, registered on `BaseIT`, that
  writes one `<TestClass>.java.<scenario-slug>.genseq.puml` (+ `.genseq.json` details) per
  traced test.
- Selection of untagged tests by human-review (`GENSEQ_SELECT`), so a branch's new tests
  are drawn without anyone tagging them.
- `human-review.json` `steps.sequence` (+ `c2.containers`) configured so the tab and the
  container view render.

## Non-goals

- Exporting traces anywhere in production (no collector, no Tempo, no Grafana). The OTLP
  exporter may be on the classpath; it is not enabled by default.
- The Grafana trace screenshot on the Sequence tab (`"trace": false`; the tab shows the
  skill's example).
- Async/multi-thread propagation beyond what Micrometer gives for free (the code on `main`
  is entirely synchronous).
- Tracing unit tests (only `BaseIT` subclasses).

## Anti-goals

- Diagrams that change on every run (random UUIDs, timestamps, span ids in the files).
  They would fail CI's "Check generated files are up to date" step and drown the diff.
- Tracing that alters production behaviour or meaningfully slows requests.
- Every mapper and config bean as a step: a diagram nobody can read is worse than none.

## Constraints

- Spring Boot 4.0.6, Java 25, Spring AI 2.0.0-M6, Spring Modulith 2.0.6 (with
  `spring-modulith-observability` already present).
- Checkstyle + PMD at compile (JUnit classes package-private); ArchUnit: nothing outside
  `api` depends on `api`, Swagger annotations only in `api`.
- CI regenerates and diffs committed generated files (`docs/generated/**`, `openapi.yaml`).
- Gemini `Client` and `EmbeddingModel` are `@MockitoBean`s in ITs; Piper is WireMock.
- `PiperTextToSpeechRestService` builds its `RestClient` with the static
  `RestClient.builder()`; it must use the auto-configured `RestClient.Builder` bean so the
  client observation is applied.

## Approaches

1. **Test-only call recorder** — a test-scope AOP aspect + Hibernate `StatementInspector`
   + WireMock journal, no tracing libraries. Smallest footprint, but a bespoke recorder
   that only exists for diagrams.
2. **Real tracing (chosen)** — Micrometer Observation bridged to OpenTelemetry, in-memory
   exporter in tests. Configured once, gives production tracing for free later, survives
   async code. The owner chose it: the setup cost is paid once, in this change, and there
   is no downside to having it in the repository.
3. **OpenTelemetry Java agent + Tempo** (as the skill's demo project does) — rejected:
   Docker and a trace store for a single-JVM MockMvc suite, and it still does not see our
   own components without extra configuration.

## Design

### Tracing in the application (`src/main`)

- Dependencies: Spring Boot 4's OpenTelemetry tracing support (the Micrometer tracing
  bridge to OpenTelemetry — the builder picks the right Boot 4 starter/module), and a
  JDBC observation library (`net.ttddyy.observation:datasource-micrometer-spring-boot`, a
  version compatible with Boot 4; fallback: a Hibernate `StatementInspector` that opens an
  observation per statement).
- `management.tracing.sampling.probability`: `1.0` in the test profile; production
  default left as is. OTLP export disabled unless an endpoint is configured.
- **Our components**: one `@Aspect` (package `com.blbulyandavbulyan.larm.observability`,
  new) wraps public methods of Spring beans under `com.blbulyandavbulyan.larm` in an
  `Observation` named `larm.component`, with low-cardinality keys `code.namespace`
  (simple class name) and `code.function` (method). Included: `@RestController`,
  `@Service`, `@Component` services/orchestrators, Spring Data repositories. Excluded:
  `@Configuration`, security filter/converter beans, `api` mappers, the observability
  package itself. The pointcut is the single place to widen or narrow it.
- Piper: `PiperTextToSpeechRestService` takes `RestClient.Builder` (bean) instead of
  `RestClient.builder()`.
- Spring AI: chat client / chat model observations are built in; with the Gemini `Client`
  mocked, the `ChatModel` observation still opens and becomes the `Gemini` lifeline.
- **Embeddings** *(builder's call)*: `EmbeddingModel` is a `@MockitoBean` in the ITs, so
  Spring AI's own embedding observation never opens. Our code (`DialogueVectorizationService`,
  `DialogueSearchService`) opens an `Observation` named `gen_ai.embedding` around each
  `embeddingModel` call, so the call is drawn as `Backend -> Gemini: embed` without
  changing how the tests mock it.
- Spring Modulith observability spans (module boundaries) are kept in the application but
  ignored by the generator, so they do not double every step.

### Capture and generation (`src/test`)

- Test config: an `InMemorySpanExporter` bean (`opentelemetry-sdk-testing`) wired into the
  tracer; cleared before each traced test.
- `@GenerateSequence` (method or class level).
- `SequenceDiagramExtension` (JUnit 5, registered on `BaseIT` via `@ExtendWith`): traces a
  test if it is annotated, or if its `Class#method` is in the `GENSEQ_SELECT` system
  property / env var (comma-separated, as human-review's `{tests.java}` passes it). After
  the test it takes the finished spans, builds the call tree from parent ids, and writes:
  - `docs/generated/sequence/<TestClass>.java.<scenario-slug>.genseq.puml`
  - `docs/generated/sequence/<TestClass>.java.<scenario-slug>.genseq.json`
  where the slug is the test's display name (or method name) lower-cased and dashed.
- **Lifelines**: `Test`, `Backend`, `DB`, `Piper`, `Gemini`. Our components are nested
  self-calls on `Backend` (as in the skill's sample), each linked
  `[[src://<repo path>:<line>{Click to open X.m} X.m ↗]]` — the line found by scanning the
  class's source for the method. Keeping components inside `Backend` keeps the C2 view
  true (it reads lifelines as containers).
- **Arrows**: `Test -> Backend: [[genseq://<id>{…} <handler>\n<VERB> <route template> ⊕]]`
  with the route template (`/dialogues/{id}`), never the concrete URL;
  `Backend -> DB` per statement (parameterised SQL, `?` placeholders); `Backend -> Piper:
  POST /...`; `Backend -> Gemini: chat`; responses `Backend --> Test: <status>`.
  `Backend -> Gemini: embed` (see Embeddings). Consecutive identical statements are
  collapsed (`×N`).
- **Only the request is drawn** *(builder's call)*: spans with no HTTP-server ancestor —
  `@Sql` fixtures, `JdbcTemplate` assertions, `TestDialogueRepository`, `BaseIT`'s cleanup —
  are dropped. No test-side given/when/then steps for now.
- **Header** exactly as the skill parses it:
  `title [[src://src/test/java/...Test.java:<line>{Click to open the test} Test: <Class>: <scenario>]]`,
  `hide footbox`, the GENERATED FILE comment, a `footer`.
- **Determinism**: `genseq://` ids are a short hash of the arrow's content and position;
  no span ids, timestamps or durations anywhere in either file; JSON keys sorted. The
  `.genseq.json` holds request method/route/body (with UUID values replaced by `<uuid>`)
  and full SQL per id, in the sample's `version: 3` shape.

### Committing and CI

- Tagged tests' diagrams are committed under `docs/generated/sequence/` and are covered by
  CI's existing generated-files check, like `DataModelDiagramsIT`'s output.
- Diagrams for untagged tests (only when `GENSEQ_SELECT` is set) are written to the same
  place during a human-review run; the skill restores committed bytes and keeps its own
  copies under `.human-review/`.
- Tag an initial set *(builder's call)*: one test each in `ChatControllerIT` (dialogue
  generation), `DialogueControllerIT` (save, and get-by-id) and `AssetsControllerIT`. There
  is no phrases endpoint or IT on `main`.

### human-review.json

```json
"sequence": {
  "select": {"max": 6, "suites": {"java": {"files": ["src/test/java/**/*IT.java"],
             "item": "{class}#{name}", "join": ",", "max": 6}}},
  "trace": false,
  "commands": [
    "mvn -B verify -Dcheckstyle.skip -Dpmd.skip -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dgroups=genseq",
    {"run": "mvn -B verify -Dcheckstyle.skip -Dpmd.skip -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dit.test={tests.java} -DGENSEQ_SELECT={tests.java}",
     "when": "java"}
  ]
},
"c2": {"containers": {"Test": {"drop": true}, "Backend": {"kind": "container"},
       "DB": {"kind": "db"}, "Piper": {"kind": "external"}, "Gemini": {"kind": "external"}}}
```

(Exact keys checked against the skill's `human-review.example.json` while building.)

## Left to the builder

- Which Boot 4 artifact provides the OpenTelemetry bridge; datasource-micrometer version.
- Whether `@AutoConfigureMockMvc` includes `ServerHttpObservationFilter` (if not, the
  extension opens the root span itself around the MockMvc call, or MockMvc is built with
  the filter).
- How the in-memory exporter is wired in (replace or add a `SpanExporter` bean).
- The exact aspect pointcut, and source-line lookup.
- How to tag tests for `-Dgroups=genseq` (`@Tag("genseq")` meta-annotated on
  `@GenerateSequence`).
