package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.BaseIT;
import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueMother;
import com.blbulyandavbulyan.larm.phrase.dao.DialogueRepository;
import com.blbulyandavbulyan.larm.phrase.dao.MediaRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DialogueControllerIT extends BaseIT {
    interface RequestMapping {
        String SAVE_DIALOGUE = "/dialogues";
        String GET_DIALOGUE = "/dialogues/{id}";
    }

    @MockitoSpyBean
    private DialogueOrchestrator dialogueOrchestrator;

    @Autowired
    private MediaRepository mediaRepository;

    @Test
    void saveDialogue() throws Exception {
        String requestJson = readResourceToString("/requests/save-dialogue-request.json");

        wireMockServer.stubFor(WireMock.post("/")
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "audio/wav")
                        .withBody(new byte[]{1, 2, 3, 4})));

        String responseContent = mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        UUID dialogueId = UUID.fromString(JsonPath.read(responseContent, "$.id"));

        var expectedDialogue = DialogueMother.DefaultDialogue.build();
        
        dialogueRecordAssertHelper.assertThatDialogueWithId(dialogueId)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsOfTypes(UUID.class, java.time.Instant.class)
                .ignoringFields("title.mediaSet", 
                        "dialoguePhrases.phrase.mediaSet", 
                        "speakers.namePhrase.mediaSet", 
                        "dialoguePhrases.speaker.namePhrase.mediaSet")
                .isEqualTo(expectedDialogue);

        // Verify media count
        long mediaCount = mediaRepository.count();
        assertThat(mediaCount).isEqualTo(6);
        //TODO in this case it might be important that the right 'audio' is connected to the 'right phrase', especially since we ignoring mediaSet completely everywhere in previous assertions
        // we should assert the media and that the right audio file is connected to the right phrase somehow here

        // Verify TTS service was called for each phrase and nothing was skipped
        verifyTtsCalledWith("Հացի փռում");
        verifyTtsCalledWith("Հացթուխ");
        verifyTtsCalledWith("Գնորդ");
        verifyTtsCalledWith("Բարեւ ձեզ");
        verifyTtsCalledWith("Բարեւ ձեզ, խնդրում եմ մեկ հաց:");
        verifyTtsCalledWith("Ահա, խնդրեմ:");
    }

    private void verifyTtsCalledWith(String text) {
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/"))
                .withRequestBody(WireMock.matchingJsonPath("$.text", WireMock.equalTo(text))));
    }

    @Test
    void saveDialogue_whenPhraseReferencesUndefinedSpeaker() throws Exception {
        String requestJson = readResourceToString("/requests/save-dialogue-with-phrase-referencing-undefined-speaker-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dialoguePhrases")
                        .value("Phrase references undefined speaker: unknown_speaker"));
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_whenDefinedSpeakerIsUnused() throws Exception {
        String requestJson = readResourceToString("/requests/save-dialogue-with-unused-defined-speaker-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.speakers")
                        .value("Defined speaker is never used: speaker2"));
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    @Sql("/sql/insert-dialogue.sql")
    void getDialogue() throws Exception {
        String expectedJson = readResourceToString("/responses/get-dialogue-response.json");
        UUID dialogueId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    @Sql("/sql/insert-dialogue.sql")
    void getDialogue_whenDialogueNotFound() throws Exception {
        UUID dialogueId = UUID.fromString("6c81f573-d19b-46ca-ad09-17b156b2eff6");

        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isNotFound());
    }
}
