package com.blbulyandavbulyan.larm.genseq;

import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanTreeTest {
    private static final String TRACE = "0123456789abcdef0123456789abcdef";
    private static final String PARENT_NONE = "0000000000000000";
    private static final String CONTROLLER = "com.blbulyandavbulyan.larm.api.dialogues.DialogueController";
    private static final String SERVICE = "com.blbulyandavbulyan.larm.core.DialogueOrchestrator";

    private long clock;

    private static String spanId(String id) {
        return "%016x".formatted(id.hashCode() & 0xffffffffL);
    }

    private SpanData span(String id, String parent, String name, SpanKind kind, String... attrs) {
        AttributesBuilder builder = Attributes.builder();
        if (attrs.length % 2 != 0) {
            throw new IllegalArgumentException("attrs must be key/value pairs, got " + attrs.length + " elements");
        }
        for (int i = 0; i + 1 < attrs.length; i += 2) {
            builder.put(attrs[i], attrs[i + 1]);
        }
        long start = ++clock;
        return TestSpanData.builder()
                .setSpanContext(SpanContext.create(TRACE, spanId(id), TraceFlags.getSampled(), TraceState.getDefault()))
                .setParentSpanContext(PARENT_NONE.equals(parent)
                        ? SpanContext.getInvalid()
                        : SpanContext.create(TRACE, spanId(parent), TraceFlags.getSampled(), TraceState.getDefault()))
                .setName(name)
                .setKind(kind)
                .setStartEpochNanos(start)
                .setEndEpochNanos(start + 1)
                .setHasEnded(true)
                .setStatus(StatusData.ok())
                .setAttributes(builder.build())
                .build();
    }

    private SpanData server(String id, String method, String route, String status) {
        return span(id, PARENT_NONE, "http " + method.toLowerCase(java.util.Locale.ROOT) + " " + route, SpanKind.SERVER,
                "uri", route, "method", method, "status", status);
    }

    private SpanData component(String id, String parent, String namespace, String function) {
        return span(id, parent, SpanNames.COMPONENT_SPAN, SpanKind.INTERNAL,
                SpanNames.NAMESPACE_KEY, namespace, SpanNames.FUNCTION_KEY, function);
    }

    private SpanData sql(String id, String parent, String statement) {
        return span(id, parent, SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, SpanNames.JDBC_SQL_KEY, statement);
    }

    @Test
    void dropsSpansOutsideAnyRequest() {
        var spans = List.of(component("a1", PARENT_NONE, SERVICE, "orphan"), sql("a2", "a1", "select 1"));

        assertThat(SpanTree.requests(spans)).isEmpty();
    }

    @Test
    void twoRequestsInOneTestAreTwoRootsInOrder() {
        var first = server("b1", "POST", "/dialogues", "200");
        var second = server("b2", "GET", "/dialogues/{id}", "200");
        var spans = new ArrayList<>(List.of(second, first));

        var requests = SpanTree.requests(spans);

        assertThat(requests).extracting(Call::detail).containsExactly("POST /dialogues", "GET /dialogues/{id}");
    }

    @Test
    void foldsControllerSpanIntoRequestLabel() {
        var spans = List.of(
                server("c1", "POST", "/dialogues", "200"),
                component("c2", "c1", CONTROLLER, "saveDialogue"),
                component("c3", "c2", SERVICE, "saveDialogue"));

        var request = SpanTree.requests(spans).getFirst();

        assertThat(request.kind()).isEqualTo(Call.Kind.REQUEST);
        assertThat(request.label()).isEqualTo("saveDialogue");
        assertThat(request.detail()).isEqualTo("POST /dialogues");
        assertThat(request.children()).extracting(Call::label).containsExactly(SERVICE + ".saveDialogue");
    }

    @Test
    void errorResponseKeepsStatus() {
        var spans = List.of(server("d1", "POST", "/dialogues", "400"));

        assertThat(SpanTree.requests(spans).getFirst().attributes()).containsEntry("status", "400");
    }

    @Test
    void collapsesConsecutiveIdenticalSql() {
        var spans = List.of(
                server("e1", "GET", "/x", "200"),
                sql("e2", "e1", "select a"), sql("e3", "e1", "select a"), sql("e4", "e1", "select a"),
                sql("e5", "e1", "select b"),
                sql("e6", "e1", "select a"));

        var children = SpanTree.requests(spans).getFirst().children();

        assertThat(children).extracting(Call::label).containsExactly("select a", "select b", "select a");
        assertThat(children.getFirst().attributes()).containsEntry("count", "3");
        assertThat(children.get(1).attributes()).doesNotContainKey("count");
    }

    @Test
    void batchedStatementsBecomeOneSqlCallEach() {
        var spans = List.of(
                server("f1", "GET", "/x", "200"),
                span("f2", "f1", SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, "jdbc.query[1]", "insert b", "jdbc.query[0]", "insert a"));

        assertThat(SpanTree.requests(spans).getFirst().children()).extracting(Call::label)
                .containsExactly("insert a", "insert b");
    }

    @Test
    void springAiChatClientSpanIsAGeminiCallAndMockitoSuffixIsDropped() {
        var spans = List.of(
                server("m1", "POST", "/chat", "200"),
                component("m2", "m1", "DialogueRepository$MockitoMock$EuHcSi1w", "save"),
                span("m3", "m1", "spring_ai chat_client", SpanKind.INTERNAL, "spring.ai.kind", "chat_client"));

        var children = SpanTree.requests(spans).getFirst().children();

        assertThat(children).extracting(Call::kind, Call::label)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(Call.Kind.COMPONENT, "DialogueRepository.save"),
                        org.assertj.core.groups.Tuple.tuple(Call.Kind.GEMINI_CHAT, "spring_ai chat_client"));
    }

    @Test
    void chatClientDescendantsAreOneGeminiCall() {
        var spans = List.of(
                server("g1", "POST", "/chat", "200"),
                span("g2", "g1", "spring.ai.chat.client", SpanKind.INTERNAL),
                span("g3", "g2", "spring.ai.advisor", SpanKind.INTERNAL),
                span("g4", "g3", "chat gemini", SpanKind.CLIENT, "gen_ai.operation.name", "chat"),
                span("g5", "g4", "http post", SpanKind.CLIENT, "method", "POST"));

        var children = SpanTree.requests(spans).getFirst().children();

        assertThat(children).hasSize(1);
        assertThat(children.getFirst().kind()).isEqualTo(Call.Kind.GEMINI_CHAT);
        assertThat(children.getFirst().children()).isEmpty();
    }

    @Test
    void classifiesPiperEmbeddingAndChatModelBeforeClientFallback() {
        var spans = List.of(
                server("h1", "POST", "/x", "200"),
                span("h2", "h1", "http post", SpanKind.CLIENT, "method", "POST", "status", "200",
                        "http.url", "http://localhost:5000/api/tts?x=1"),
                span("h3", "h1", SpanNames.EMBEDDING_SPAN, SpanKind.INTERNAL),
                span("h4", "h1", "chat gemini-x", SpanKind.CLIENT, "gen_ai.operation.name", "chat"));

        assertThat(SpanTree.requests(spans).getFirst().children()).extracting(Call::kind, Call::label)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(Call.Kind.PIPER, "POST /api/tts"),
                        org.assertj.core.groups.Tuple.tuple(Call.Kind.GEMINI_EMBED, SpanNames.EMBEDDING_SPAN),
                        org.assertj.core.groups.Tuple.tuple(Call.Kind.GEMINI_CHAT, "chat gemini-x"));
    }

    @Test
    void unknownSpansAreReparentedNotLost() {
        var spans = List.of(
                server("i1", "GET", "/x", "200"),
                span("i2", "i1", "spring.security.filterchains", SpanKind.INTERNAL),
                component("i3", "i2", SERVICE, "work"),
                span("i4", "i3", "transaction", SpanKind.INTERNAL),
                sql("i5", "i4", "select 1"));

        var children = SpanTree.requests(spans).getFirst().children();

        assertThat(children).extracting(Call::label).containsExactly(SERVICE + ".work");
        assertThat(children.getFirst().children()).extracting(Call::label).containsExactly("select 1");
    }

    @Test
    void sqlCountsAreAddedWhenControllerChildrenAreFoldedIntoTheRequest() {
        var spans = List.of(
                server("k1", "GET", "/x", "200"),
                component("k2", "k1", CONTROLLER, "get"),
                span("k3", "k2", SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, SpanNames.JDBC_SQL_KEY, "select a"),
                span("k4", "k2", SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, SpanNames.JDBC_SQL_KEY, "select a"),
                span("k5", "k2", SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, SpanNames.JDBC_SQL_KEY, "select a"),
                span("k6", "k1", SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, SpanNames.JDBC_SQL_KEY, "select a"));

        var children = SpanTree.requests(spans).getFirst().children();

        assertThat(children).hasSize(1);
        assertThat(children.getFirst().attributes()).containsEntry("count", "4");
    }

    @Test
    void clientSpanWithoutHttpMethodIsLookedThrough() {
        var spans = List.of(
                server("m1", "GET", "/x", "200"),
                span("m2", "m1", "connection", SpanKind.CLIENT),
                span("m3", "m2", SpanNames.JDBC_SPAN_NAME, SpanKind.CLIENT, SpanNames.JDBC_SQL_KEY, "select 1"));

        assertThat(SpanTree.requests(spans).getFirst().children()).extracting(Call::kind, Call::label)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(Call.Kind.SQL, "select 1"));
    }
}
