package com.blbulyandavbulyan.larm.genseq;

import java.util.List;

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
    @Test
    void saveDialogue_producesServerSpanWithRouteAndPiperClientSpanInSameTrace() throws Exception {
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

        List<SpanData> spans = TracingTestConfiguration.finishedSpans(applicationContext);
        SpanData server = spans.stream().filter(s -> s.getKind() == SpanKind.SERVER).findFirst().orElseThrow();
        assertThat(server.getAttributes().get(AttributeKey.stringKey(SpanNames.ROUTE_KEY))).isEqualTo("/dialogues");
        assertThat(spans).anySatisfy(s -> {
            assertThat(s.getKind()).isEqualTo(SpanKind.CLIENT);
            assertThat(s.getTraceId()).isEqualTo(server.getTraceId());
        });
    }
}
