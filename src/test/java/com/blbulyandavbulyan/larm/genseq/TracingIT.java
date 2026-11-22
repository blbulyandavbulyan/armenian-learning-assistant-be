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
        assertThat(sqls).anyMatch(sql -> sql.toLowerCase(Locale.ROOT).startsWith("insert into"));
        assertThat(sqls).noneMatch(sql -> UUID_PREFIX.matcher(sql).find());
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
