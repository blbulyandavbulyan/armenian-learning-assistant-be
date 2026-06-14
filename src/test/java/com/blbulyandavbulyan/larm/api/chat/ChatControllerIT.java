package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.PhrasesChatService;
import com.blbulyandavbulyan.larm.ai.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftPhraseResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResource;
import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;
import com.blbulyandavbulyan.larm.api.BaseIT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerIT extends BaseIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PhrasesChatService phrasesChatService;

    @Test
    void phrasesChat_success() throws Exception {
        UUID chatId = UUID.randomUUID();
        String message = "Help me buy bread";

        DraftTranslationResource translation = new DraftTranslationResource("Где находится хлеб?", "ru");
        DraftPhraseResource phrase = new DraftPhraseResource(
                "Որտե՞ղ է հացը:",
                "hy",
                "Vortegh e hatsy?",
                List.of(translation)
        );
        StructuredPhrasesResource serviceResponse = new StructuredPhrasesResource(
                "Here is the phrase",
                List.of(phrase)
        );

        when(phrasesChatService.phrasesChat(message, chatId)).thenReturn(serviceResponse);

        ChatRequest request = new ChatRequest(message, chatId);

        mockMvc.perform(post("/chat/phrases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Here is the phrase"))
                .andExpect(jsonPath("$.phrases[0].phrase").value("Որտե՞ղ է հացը:"))
                .andExpect(jsonPath("$.phrases[0].isoLanguageCode").value("hy"))
                .andExpect(jsonPath("$.phrases[0].transcription").value("Vortegh e hatsy?"))
                .andExpect(jsonPath("$.phrases[0].translations[0].translationText").value("Где находится хлеб?"))
                .andExpect(jsonPath("$.phrases[0].translations[0].isoLanguageCode").value("ru"));
    }

    @Test
    void phrasesChat_validationFailure() throws Exception {
        ChatRequest invalidRequest = new ChatRequest("", null);

        mockMvc.perform(post("/chat/phrases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists())
                .andExpect(jsonPath("$.errors.chatId").exists());
    }

    @Test
    void dialogueChat_success() throws Exception {
        UUID chatId = UUID.randomUUID();
        String message = "Create a shop dialogue";

        StructuredDialogueResource.DialogueTitleResource info = new StructuredDialogueResource.DialogueTitleResource(
                "Խանութում",
                "Khanutum",
                List.of(new DraftTranslationResource("В магазине", "ru"))
        );

        StructuredDialogueResource.SpeakerResource speaker = new StructuredDialogueResource.SpeakerResource(
                "speaker-1",
                "Գնորդ",
                "Gnord",
                List.of(new DraftTranslationResource("Покупатель", "ru"))
        );

        DraftPhraseResource phrase = new DraftPhraseResource(
                "Բարև ձեզ",
                "hy",
                "Barev dzez",
                List.of(new DraftTranslationResource("Здравствуйте", "ru"))
        );

        StructuredDialogueResource.DialoguePhrase dialoguePhrase = new StructuredDialogueResource.DialoguePhrase(
                "speaker-1",
                phrase
        );

        StructuredDialogueResource serviceResponse = new StructuredDialogueResource(
                info,
                "Dialogue generated",
                List.of(speaker),
                List.of(dialoguePhrase)
        );

        when(phrasesChatService.dialogueChat(message, chatId)).thenReturn(serviceResponse);

        ChatRequest request = new ChatRequest(message, chatId);

        mockMvc.perform(post("/chat/dialogue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dialogue generated"))
                .andExpect(jsonPath("$.info.title").value("Խանութում"))
                .andExpect(jsonPath("$.info.transcription").value("Khanutum"))
                .andExpect(jsonPath("$.info.translations[0].translationText").value("В магазине"))
                .andExpect(jsonPath("$.info.translations[0].isoLanguageCode").value("ru"))
                .andExpect(jsonPath("$.speakers[0].id").value("speaker-1"))
                .andExpect(jsonPath("$.speakers[0].title").value("Գնորդ"))
                .andExpect(jsonPath("$.speakers[0].transcription").value("Gnord"))
                .andExpect(jsonPath("$.speakers[0].translations[0].translationText").value("Покупатель"))
                .andExpect(jsonPath("$.dialoguePhrases[0].speakerId").value("speaker-1"))
                .andExpect(jsonPath("$.dialoguePhrases[0].phrase.phrase").value("Բարև ձեզ"));
    }

    @Test
    void dialogueChat_validationFailure() throws Exception {
        ChatRequest invalidRequest = new ChatRequest("   ", null);

        mockMvc.perform(post("/chat/dialogue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists())
                .andExpect(jsonPath("$.errors.chatId").exists());
    }
}
