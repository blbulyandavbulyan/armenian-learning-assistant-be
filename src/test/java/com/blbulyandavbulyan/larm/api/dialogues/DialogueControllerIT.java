package com.blbulyandavbulyan.larm.api.dialogues;

import java.nio.file.Files;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.BaseIT;
import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueMother;
import com.blbulyandavbulyan.larm.dialogue.dao.TestDialogueRepository;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueTitlePhrase.PHRASE, new byte[]{1});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE, new byte[]{2});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE, new byte[]{3});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase1.PHRASE, new byte[]{4});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase2.PHRASE, new byte[]{5});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase3.PHRASE, new byte[]{6});

        when(embeddingModel.embed(PhraseMother.DialogueTitlePhrase.EMBEDDING_TEXT))
                .thenReturn(PhraseMother.DialogueTitlePhrase.embedding());

        when(embeddingModel.embed(PhraseMother.DialogueSpeaker1NamePhrase.EMBEDDING_TEXT))
                .thenReturn(PhraseMother.DialogueSpeaker1NamePhrase.embedding());

        when(embeddingModel.embed(PhraseMother.DialogueSpeaker2NamePhrase.EMBEDDING_TEXT))
                .thenReturn(PhraseMother.DialogueSpeaker2NamePhrase.embedding());

        when(embeddingModel.embed(PhraseMother.DialoguePhrase1.EMBEDDING_TEXT))
                .thenReturn(PhraseMother.DialoguePhrase1.embedding());

        when(embeddingModel.embed(PhraseMother.DialoguePhrase2.EMBEDDING_TEXT))
                .thenReturn(PhraseMother.DialoguePhrase2.embedding());

        when(embeddingModel.embed(PhraseMother.DialoguePhrase3.EMBEDDING_TEXT))
                .thenReturn(PhraseMother.DialoguePhrase3.embedding());

        when(embeddingModel.embed(DialogueMother.DefaultDialogue.EMBEDDING_TEXT))
                .thenReturn(DialogueMother.DefaultDialogue.embedding());

        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-request.json");
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

        assertThat(dialogue.getTitle().getMediaSet()).hasSize(1);
        Media titleMedia = dialogue.getTitle().getMediaSet().iterator().next();
        assertThat(readMediaBytes(titleMedia)).isEqualTo(new byte[]{1});

        assertThat(dialogue.getSpeakers())
                .anySatisfy(speaker -> {
                    assertThat(speaker.getNamePhrase().getPhrase()).isEqualTo(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE);
                    assertThat(speaker.getNamePhrase().getMediaSet()).hasSize(1);
                    assertThat(readMediaBytes(speaker.getNamePhrase().getMediaSet().iterator().next())).isEqualTo(new byte[]{2});
                })
                .anySatisfy(speaker -> {
                    assertThat(speaker.getNamePhrase().getPhrase()).isEqualTo(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE);
                    assertThat(speaker.getNamePhrase().getMediaSet()).hasSize(1);
                    assertThat(readMediaBytes(speaker.getNamePhrase().getMediaSet().iterator().next())).isEqualTo(new byte[]{3});
                });

        assertThat(dialogue.getDialoguePhrases())
                .anySatisfy(dp -> {
                    assertThat(dp.getPhrase().getPhrase()).isEqualTo(PhraseMother.DialoguePhrase1.PHRASE);
                    assertThat(dp.getPhrase().getMediaSet()).hasSize(1);
                    assertThat(readMediaBytes(dp.getPhrase().getMediaSet().iterator().next())).isEqualTo(new byte[]{4});
                })
                .anySatisfy(dp -> {
                    assertThat(dp.getPhrase().getPhrase()).isEqualTo(PhraseMother.DialoguePhrase2.PHRASE);
                    assertThat(dp.getPhrase().getMediaSet()).hasSize(1);
                    assertThat(readMediaBytes(dp.getPhrase().getMediaSet().iterator().next())).isEqualTo(new byte[]{5});
                })
                .anySatisfy(dp -> {
                    assertThat(dp.getPhrase().getPhrase()).isEqualTo(PhraseMother.DialoguePhrase3.PHRASE);
                    assertThat(dp.getPhrase().getMediaSet()).hasSize(1);
                    assertThat(readMediaBytes(dp.getPhrase().getMediaSet().iterator().next())).isEqualTo(new byte[]{6});
                });

        // Verify TTS service was called for each phrase and nothing was skipped
        piperWireMock.verifyTtsCalledWith(com.blbulyandavbulyan.larm.phrase.dao.PhraseMother.DialogueTitlePhrase.PHRASE);
        piperWireMock.verifyTtsCalledWith(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE);
        piperWireMock.verifyTtsCalledWith(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE);
        piperWireMock.verifyTtsCalledWith(PhraseMother.DialoguePhrase1.PHRASE);
        piperWireMock.verifyTtsCalledWith(PhraseMother.DialoguePhrase2.PHRASE);
        piperWireMock.verifyTtsCalledWith(PhraseMother.DialoguePhrase3.PHRASE);
    }

    private byte[] readMediaBytes(Media media) {
        try {
            return Files.readAllBytes(TEMP_DIR.resolve(media.getStorageKey()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveDialogue_withInvalidFields() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-invalid-fields-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(15)))
                .andExpect(jsonPath("$.errors['info.title']").exists())
                .andExpect(jsonPath("$.errors['info.transcription']").exists())
                .andExpect(jsonPath("$.errors['info.translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['info.translations[0].isoLanguageCode']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].id']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].title']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].transcription']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].translations[0].isoLanguageCode']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].speakerId']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.phrase']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.isoLanguageCode']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.transcription']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.translations[0].isoLanguageCode']").exists());
        
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_withNullRootFieldsFields() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-missing-fields-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(3)))
                .andExpect(jsonPath("$.errors.info").exists())
                .andExpect(jsonPath("$.errors.speakers").exists())
                .andExpect(jsonPath("$.errors.dialoguePhrases").exists());
        
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_withEmptySpeakersAndEmptyDialoguePhrases() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-empty-speakers-and-dialogue-phrases-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(3)))
                .andExpect(jsonPath("$.errors.info").exists())
                .andExpect(jsonPath("$.errors.speakers").exists())
                .andExpect(jsonPath("$.errors.dialoguePhrases").exists());

        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_withEmptyTranslations() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-empty-translations-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(3)))
                .andExpect(jsonPath("$.errors['info.translations']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].translations']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.translations']").exists());
        
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_withNullFieldsAndNonEmptyLists() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-with-null-fields-not-empty-lists-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(15)))
                .andExpect(jsonPath("$.errors['info.title']").exists())
                .andExpect(jsonPath("$.errors['info.transcription']").exists())
                .andExpect(jsonPath("$.errors['info.translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['info.translations[0].isoLanguageCode']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].id']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].title']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].transcription']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['speakers[0].translations[0].isoLanguageCode']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].speakerId']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.phrase']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.isoLanguageCode']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.transcription']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['dialoguePhrases[0].phrase.translations[0].isoLanguageCode']").exists());

        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_whenPhraseReferencesUndefinedSpeaker() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-with-phrase-referencing-undefined-speaker-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$.errors.dialoguePhrases")
                        .value("Phrase references undefined speaker: unknown_speaker"));
        verifyNoInteractions(dialogueOrchestrator);
    }

    @Test
    void saveDialogue_whenDefinedSpeakerIsUnused() throws Exception {
        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-with-unused-defined-speaker-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.aMapWithSize(1)))
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
