package com.blbulyandavbulyan.larm.genseq;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueMother;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockDatabaseUser
class TracingIT extends BaseIT {
    private static final Pattern UUID_PREFIX = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-");

    @Test
    void saveDialogue_producesServerSpanWithRouteAndPiperClientSpanInSameTrace() throws Exception {
        saveDialogue();

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        SpanData server = spans.stream().filter(s -> s.getKind() == SpanKind.SERVER).findFirst().orElseThrow();
        assertThat(server.getAttributes().get(AttributeKey.stringKey(SpanNames.ROUTE_KEY))).isEqualTo("/dialogues");
        assertThat(spans).anySatisfy(s -> {
            assertThat(s.getKind()).isEqualTo(SpanKind.CLIENT);
            assertThat(s.getTraceId()).isEqualTo(server.getTraceId());
        });
    }

    @Test
    void saveDialogue_producesSqlSpansInsideTheRequest() throws Exception {
        saveDialogue();

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        SpanData server = spans.stream().filter(s -> s.getKind() == SpanKind.SERVER).findFirst().orElseThrow();
        List<String> sqls = spans.stream()
                .filter(s -> s.getTraceId().equals(server.getTraceId()))
                .map(s -> s.getAttributes().get(AttributeKey.stringKey(SpanNames.JDBC_SQL_KEY)))
                .filter(Objects::nonNull)
                .toList();
        assertThat(sqls)
                .anyMatch(sql -> sql.toLowerCase(Locale.ROOT).startsWith("insert into"))
                .noneMatch(sql -> UUID_PREFIX.matcher(sql).find());
    }

    @Test
    void saveDialogue_producesComponentSpans() throws Exception {
        saveDialogue();

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        SpanData orchestrator = componentSpan(spans, "DialogueOrchestrator", "saveDialogue");
        SpanData processor = spans.stream()
                .filter(s -> "PhraseProcessor".equals(namespace(s)))
                .findFirst().orElseThrow();
        assertThat(hasAncestor(spans, processor, orchestrator)).isTrue();
        assertThat(spans).anyMatch(s -> namespace(s) != null && namespace(s).endsWith("Repository"));
    }

    @Test
    void saveDialogue_insertSpansAreInsideDialogueSavingServiceSpan() throws Exception {
        saveDialogue();

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        SpanData saving = componentSpan(spans, "DialogueSavingService", "saveDialogue");
        List<SpanData> inserts = spans.stream()
                .filter(s -> {
                    String sql = s.getAttributes().get(AttributeKey.stringKey(SpanNames.JDBC_SQL_KEY));
                    return sql != null && sql.toLowerCase(Locale.ROOT).startsWith("insert into");
                })
                .toList();
        assertThat(inserts).isNotEmpty().allSatisfy(insert -> assertThat(hasAncestor(spans, insert, saving)).isTrue());
    }

    @Test
    void saveDialogue_producesEmbeddingSpan() throws Exception {
        saveDialogue();

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        SpanData server = spans.stream().filter(s -> s.getKind() == SpanKind.SERVER).findFirst().orElseThrow();
        assertThat(spans).anyMatch(s -> SpanNames.EMBEDDING_SPAN.equals(s.getName())
                && s.getTraceId().equals(server.getTraceId()));
    }

    @Test
    void componentSpans_excludeMappersAndConfiguration() throws Exception {
        saveDialogue();

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        List<String> namespaces = spans.stream()
                .filter(s -> SpanNames.COMPONENT_SPAN.equals(s.getName()))
                .map(TracingIT::namespace)
                .toList();
        assertThat(namespaces).isNotEmpty()
                .noneMatch(n -> n.endsWith("Mapper") || n.contains("Configuration") || n.contains("Security"));
    }

    private static String namespace(SpanData span) {
        return span.getAttributes().get(AttributeKey.stringKey(SpanNames.NAMESPACE_KEY));
    }

    private static SpanData componentSpan(List<SpanData> spans, String namespace, String function) {
        return spans.stream()
                .filter(s -> namespace.equals(namespace(s))
                        && function.equals(s.getAttributes().get(AttributeKey.stringKey(SpanNames.FUNCTION_KEY))))
                .findFirst().orElseThrow();
    }

    private static boolean hasAncestor(List<SpanData> spans, SpanData span, SpanData ancestor) {
        String parentId = span.getParentSpanId();
        while (SpanId.isValid(parentId)) {
            if (parentId.equals(ancestor.getSpanId())) {
                return true;
            }
            String current = parentId;
            parentId = spans.stream().filter(s -> s.getSpanId().equals(current))
                    .map(SpanData::getParentSpanId).findFirst().orElse(SpanId.getInvalid());
        }
        return false;
    }

    private void saveDialogue() throws Exception {
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueTitlePhrase.PHRASE, new byte[]{1});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE, new byte[]{2, 3});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE, new byte[]{3, 4, 5});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase1.PHRASE, new byte[]{4});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase2.PHRASE, new byte[]{5});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase3.PHRASE, new byte[]{6});
        when(embeddingModel.embed(DialogueMother.DefaultDialogue.EMBEDDING_TEXT))
                .thenReturn(DialogueMother.DefaultDialogue.embedding());

        mockMvc.perform(post("/dialogues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readResourceToString("/requests/dialogue/save/save-dialogue-request.json")))
                .andExpect(status().isCreated());
    }
}
