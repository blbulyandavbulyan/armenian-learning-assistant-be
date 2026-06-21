package com.blbulyandavbulyan.larm.api.phrases;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.BaseIT;
import com.blbulyandavbulyan.larm.phrase.dao.MediaMother;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.blbulyandavbulyan.larm.phrase.dao.TranslationMother;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.transaction.annotation.Transactional;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PhrasesControllerSavePhrasesIT extends BaseIT {

    @Test
    @Transactional
    void savePhrases() throws Exception {
        String request = readResourceToString("/requests/save-phrases-request.json");
        String expectedResponse = readResourceToString("/responses/save-phrases-response.json");

        final UUID phraseId = PhraseMother.DefaultPhrase.ID;
        final UUID translationId = TranslationMother.DefaultTranslation.ID;
        final UUID mediaId = MediaMother.DefaultMedia.ID;

        final byte[] expectedMediaBinaryContent = {1, 2, 3, 4};
        wireMockServer.stubFor(WireMock.post("/")
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .withRequestBody(equalToJson("{\"text\":\"Որտե՞ղ է հացի բաժինը:\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "audio/wav")
                        .withBody(expectedMediaBinaryContent)));

        try (
                var uuidMockedStatic = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS);
                var instantMockedStatic = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {

            uuidMockedStatic.when(UUID::randomUUID)
                    .thenReturn(phraseId, mediaId, translationId);

            instantMockedStatic.when(Instant::now)
                    .thenReturn(TranslationMother.DefaultTranslation.CREATED_AT, MediaMother.DefaultMedia.CREATED_AT);

            mockMvc.perform(post(RequestMapping.SAVE_PHRASES)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));

            Mockito.verify(phraseOrchestrator, times(1)).savePhrases(anyList());

            phraseRecordAssertHelper.assertThatPhraseWithId(phraseId)
                    .as("Checking saved phrase in the database")
                    .isPresent()
                    .get()
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(PhraseMother.DefaultPhrase.builder()
                            .withMedias(MediaMother.DefaultMedia.builder()
                                    .withStorageBucket(TEMP_DIR.toString())
                                    .build())
                            .build());

            assertThat(TEMP_DIR.resolve(MediaMother.DefaultMedia.STORAGE_KEY).toFile())
                    .as("Checking the saved media file")
                    .exists()
                    .hasBinaryContent(expectedMediaBinaryContent);

        }
    }

    @Test
    void savePhrases_whenInvalidRequest() throws Exception {
        String request = readResourceToString("/requests/save-phrases-invalid-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_PHRASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors['phrases[0].phrase']").exists())
                .andExpect(jsonPath("$.errors['phrases[0].transcription']").exists())
                .andExpect(jsonPath("$.errors['phrases[0].translations[0].translationText']").exists())
                .andExpect(jsonPath("$.errors['phrases[0].translations[0].isoLanguageCode']").exists());
        verifyNoInteractions(phraseOrchestrator);
    }

    @Test
    void savePhrases_whenPhrasesListIsEmpty() throws Exception {
        String request = readResourceToString("/requests/save-phrases-empty-phrases-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_PHRASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors['phrases']").exists());
        verifyNoInteractions(phraseOrchestrator);
    }

    @Test
    void savePhrases_whenTranslationsListIsEmpty() throws Exception {
        String request = readResourceToString("/requests/save-phrases-empty-translations-request.json");

        mockMvc.perform(post(RequestMapping.SAVE_PHRASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors['phrases[0].translations']").exists());
        verifyNoInteractions(phraseOrchestrator);
    }
}
