package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.BaseIT;
import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dialogue.dao.Dialogue;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueRepository;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueSpeakerRepository;
import com.blbulyandavbulyan.larm.phrase.dao.MediaRepository;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseRepository;
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
    private DialogueRepository dialogueRepository;

    @Autowired
    private DialogueSpeakerRepository dialogueSpeakerRepository;

    @Autowired
    private PhraseRepository phraseRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Test
    void saveDialogue() throws Exception {
        String requestJson = readResourceToString("/responses/save-dialogue-request.json");

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

        String idString = JsonPath.read(responseContent, "$.id");
        UUID dialogueId = UUID.fromString(idString);

        // TODO dumb assert, very week, not all the input data is asserted
        // Verify DB
        Dialogue savedDialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new AssertionError("Dialogue not found"));

        // 1. Verify title
        Phrase titlePhrase = phraseRepository.findById(savedDialogue.titlePhraseId()).orElseThrow();
        assertThat(titlePhrase.phrase()).isEqualTo("In the bakery");

        // 2. Verify speakers
        assertThat(savedDialogue.speakers()).hasSize(2);
        long speakerCount = dialogueSpeakerRepository.count();
        assertThat(speakerCount).isEqualTo(2);

        boolean foundBaker = false;
        boolean foundCustomer = false;
        for (var speaker : savedDialogue.speakers()) {
            Phrase namePhrase = phraseRepository.findById(speaker.namePhraseId()).orElseThrow();
            if (namePhrase.phrase().equals("Baker")) {
                foundBaker = true;
                assertThat(speaker.speakerRefId()).isEqualTo("speaker1");
            } else if (namePhrase.phrase().equals("Customer")) {
                foundCustomer = true;
                assertThat(speaker.speakerRefId()).isEqualTo("speaker2");
            }
        }
        assertThat(foundBaker).isTrue();
        assertThat(foundCustomer).isTrue();

        // 3. Verify dialogue phrases
        assertThat(savedDialogue.dialoguePhrases()).hasSize(3);

        // Total phrases = 1 title + 2 speakers + 3 phrases = 6
        long phraseCount = phraseRepository.count();
        assertThat(phraseCount).isEqualTo(6);

        // Total media = 6
        long mediaCount = mediaRepository.count();
        assertThat(mediaCount).isEqualTo(6);
    }

    @Test
    void saveDialogue_whenPhraseReferencesUndefinedSpeaker() throws Exception {
        String requestJson = readResourceToString("/responses/save-dialogue-with-phrase-referencing-undefined-speaker-request.json");

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
        String requestJson = readResourceToString("/responses/save-dialogue-with-unused-defined-speaker-request.json");

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
