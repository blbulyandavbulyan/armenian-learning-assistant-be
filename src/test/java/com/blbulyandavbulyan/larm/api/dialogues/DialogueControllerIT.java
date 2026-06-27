package com.blbulyandavbulyan.larm.api.dialogues;

import java.nio.file.Files;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.BaseIT;
import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueMother;
import com.blbulyandavbulyan.larm.phrase.dao.TestDialogueRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;

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
    private TestDialogueRepository testDialogueRepository;

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

        var dialogue = testDialogueRepository.findByIdEagerly(dialogueId).orElseThrow();
        // TODO, one audio per everything,
        //  we should return different audio for different phrase and assert
        //  that associations work correctly there. Most probably we have to mock that via wiremock properly, with proper request bodies there
        byte[] expectedAudio = new byte[]{1, 2, 3, 4};
        
        Media titleMedia = dialogue.getTitle().getMediaSet().iterator().next();
        assertThat(Files.readAllBytes(TEMP_DIR.resolve(titleMedia.getStorageKey())))
                .isEqualTo(expectedAudio);

        // TODO user natural assertj assertions for this, you can chain stuff
        dialogue.getSpeakers().forEach(speaker -> {
            Media speakerMedia = speaker.getNamePhrase().getMediaSet().iterator().next();
            try {
                assertThat(Files.readAllBytes(TEMP_DIR.resolve(speakerMedia.getStorageKey())))
                        .isEqualTo(expectedAudio);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // TODO user natural assertj assertions for this, you can chain stuff
        dialogue.getDialoguePhrases().forEach(dp -> {
            Media phraseMedia = dp.getPhrase().getMediaSet().iterator().next();
            try {
                assertThat(Files.readAllBytes(TEMP_DIR.resolve(phraseMedia.getStorageKey())))
                        .isEqualTo(expectedAudio);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

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
    void saveDialogue_whenInfoIsNull() throws Exception {
        String requestJson = readResourceToString("/requests/save-dialogue-request.json");
        String invalidJson = JsonPath.parse(requestJson).delete("$.info").jsonString();

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.info").exists());
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_whenTitleIsBlank() throws Exception {
        String requestJson = readResourceToString("/requests/save-dialogue-request.json");
        String invalidJson = JsonPath.parse(requestJson).set("$.info.title", " ").jsonString();

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['info.title']").exists());
        verifyNoInteractions(dialogueOrchestrator);
    }

    // TODO we probably should have 2 of such tests,
    //  instead of individual validation tests,
    //  maybe it is worth to create one test with big invalid request,
    //  and then some small set of tests to cover uncovered cases in that test
    @Test
    void saveDialogue_whenIsoLanguageCodeIsInvalid() throws Exception {
        String requestJson = readResourceToString("/requests/save-dialogue-request.json");
        String invalidJson = JsonPath.parse(requestJson).set("$.dialoguePhrases[0].phrase.isoLanguageCode", "invalid").jsonString();

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.isoLanguageCode']").exists());
        verifyNoInteractions(dialogueOrchestrator);
    }

    // TODO not everything from validation is covered by tests

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
                .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
    }

    @Test
    void getDialogue_whenDialogueNotFound() throws Exception {
        UUID dialogueId = UUID.fromString("6c81f573-d19b-46ca-ad09-17b156b2eff6");

        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isNotFound());
    }
}
