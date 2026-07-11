package com.blbulyandavbulyan.larm.api.dialogues;

import java.nio.file.Files;
import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueMother;
import com.blbulyandavbulyan.larm.dialogue.dao.TestDialogueRepository;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class DialogueControllerIT extends BaseIT {
    interface RequestMapping {
        String SAVE_DIALOGUE = "/dialogues";
        String GET_DIALOGUE = "/dialogues/{id}";
    }

    @MockitoSpyBean
    private DialogueOrchestrator dialogueOrchestrator;

    @MockitoSpyBean
    private DialogueRepository dialogueRepository;

    @Autowired
    private TestDialogueRepository testDialogueRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveDialogue() throws Exception {
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueTitlePhrase.PHRASE, new byte[]{1});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE, new byte[]{2});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE, new byte[]{3});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase1.PHRASE, new byte[]{4});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase2.PHRASE, new byte[]{5});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase3.PHRASE, new byte[]{6});

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
        piperWireMock.verifyTtsCalledOnceWith(com.blbulyandavbulyan.larm.phrase.dao.PhraseMother.DialogueTitlePhrase.PHRASE);
        piperWireMock.verifyTtsCalledOnceWith(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE);
        piperWireMock.verifyTtsCalledOnceWith(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE);
        piperWireMock.verifyTtsCalledOnceWith(PhraseMother.DialoguePhrase1.PHRASE);
        piperWireMock.verifyTtsCalledOnceWith(PhraseMother.DialoguePhrase2.PHRASE);
        piperWireMock.verifyTtsCalledOnceWith(PhraseMother.DialoguePhrase3.PHRASE);
        verify(embeddingModel).embed(anyString());
    }

    @Test
    @Sql("/sql-test-scripts/insert-phrases-for-deduplication.sql")
    void saveDialogue_withExistingPhrases() throws Exception {
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueTitlePhrase.PHRASE, new byte[]{1});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker1NamePhrase.PHRASE, new byte[]{2});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialogueSpeaker2NamePhrase.PHRASE, new byte[]{3});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase1.PHRASE, new byte[]{4});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase2.PHRASE, new byte[]{5});
        piperWireMock.stubTtsWithAudio(PhraseMother.DialoguePhrase3.PHRASE, new byte[]{6});

        when(embeddingModel.embed(anyString()))
                .thenReturn(DialogueMother.DefaultDialogue.embedding());

        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        Long finalPhraseCount = jdbcTemplate.queryForObject("SELECT count(*) FROM phrases", Long.class);

        // Out of 6 distinct phrases in the request, 3 are already in the DB (inserted by SQL script).
        // Therefore, exactly 3 NEW phrases should be inserted.
        assertThat(finalPhraseCount).isEqualTo(6);
    }

    @Test
    void saveDialogue_withPhraseDuplicatesInOneRequest() throws Exception {
        piperWireMock.stubTtsWithAudio(PhraseMother.RealisticDialogueTitlePhrase.PHRASE, new byte[]{1});
        piperWireMock.stubTtsWithAudio(PhraseMother.RealisticDialogueSpeaker1NamePhrase.PHRASE, new byte[]{2});
        piperWireMock.stubTtsWithAudio(PhraseMother.RealisticDialogueSpeaker2NamePhrase.PHRASE, new byte[]{3});
        piperWireMock.stubTtsWithAudio(PhraseMother.RealisticDialoguePhrase1.PHRASE, new byte[]{4});
        piperWireMock.stubTtsWithAudio(PhraseMother.RealisticDialoguePhrase2.PHRASE, new byte[]{5});

        when(embeddingModel.embed(anyString()))
                .thenReturn(DialogueMother.RealisticDialogue.embedding());

        String requestJson = readResourceToString("/requests/dialogue/save/save-dialogue-with-intra-batch-duplicates-request.json");

        String responseContent = mockMvc.perform(post(RequestMapping.SAVE_DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        UUID dialogueId = UUID.fromString(JsonPath.read(responseContent, "$.id"));

        var expectedDialogue = DialogueMother.RealisticDialogue.build();

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

        Long finalPhraseCount = jdbcTemplate.queryForObject("SELECT count(*) FROM phrases", Long.class);

        // There are 6 phrase objects in the JSON payload, but "Բարեւ" is used twice.
        // Therefore, exactly 5 distinct phrases should be inserted into the database.
        assertThat(finalPhraseCount).isEqualTo(5);
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
    @Sql("/sql-test-scripts/insert-dialogue.sql")
    void getDialogue() throws Exception {
        String expectedJson = readResourceToString("/responses/get-dialogue-response.json");
        UUID dialogueId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=604800, public"))
                .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
    }

    @Test
    void getDialogue_whenDialogueNotFound() throws Exception {
        UUID dialogueId = UUID.fromString("6c81f573-d19b-46ca-ad09-17b156b2eff6");

        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql("/sql-test-scripts/insert-dialogue.sql")
    void getDialogue_cachesResults() throws Exception {
        UUID dialogueId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        // First call
        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk());

        // Second call
        mockMvc.perform(get(RequestMapping.GET_DIALOGUE, dialogueId))
                .andExpect(status().isOk());

        // Verify repository was hit only once due to caching
        verify(dialogueRepository, times(1)).findByIdEagerly(dialogueId);
    }

    private float[] generateMockEmbedding(float firstValue) {
        float[] queryEmbedding = new float[3072];
        queryEmbedding[0] = firstValue;
        return queryEmbedding;
    }

    @Test
    @Sql("/sql-test-scripts/insert-dialogues-for-search.sql")
    void searchDialogues() throws Exception {
        when(embeddingModel.embed("buying apples")).thenReturn(generateMockEmbedding(1.0f));

        String expectedJson = readResourceToString("/expected-responses/dialogue/search-dialogues-response.json");

        mockMvc.perform(get("/dialogues/search")
                .param("query", "buying apples"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
    }

    @Test
    void searchDialogues_cachesResults() throws Exception {
        when(embeddingModel.embed("hello")).thenReturn(generateMockEmbedding(1.0f));

        mockMvc.perform(get("/dialogues/search")
                .param("query", "hello"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dialogues/search")
                .param("query", "hello"))
                .andExpect(status().isOk());

        verify(embeddingModel, times(1)).embed("hello");
    }

    @Test
    void searchDialogues_emptyQuery() throws Exception {
        mockMvc.perform(get("/dialogues/search")
                .param("query", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.query").exists());
    }
}
