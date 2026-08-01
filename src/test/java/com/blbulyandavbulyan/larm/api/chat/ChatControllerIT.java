package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;
import java.util.Objects;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.ai.StructuredDialogueResourceMother;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerIT extends BaseIT {

    interface RequestMapping {
        String DIALOGUE = "/chat/dialogue";
    }

    @MockitoBean
    private ChatModel chatModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatMemory chatMemory;

    @BeforeEach
    void setUp() {
        ChatOptions mock = mock(ChatOptions.class, RETURNS_DEEP_STUBS);
        when(chatModel.getDefaultOptions()).thenReturn(mock);
    }

    @AfterEach
    void tearDown() {
        chatMemory.clear("73c68128-48b4-4e2b-b6d3-13835e5d38cc");
    }

    @Test
    void dialogueChat() throws Exception {
        StructuredDialogueResource serviceResponse = StructuredDialogueResourceMother.DefaultStructuredDialogueResource.build();
        String jsonResponse = objectMapper.writeValueAsString(serviceResponse);

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(jsonResponse)))));

        String requestJson = readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json");

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("responses/dialogue-chat-success-response.json"), JsonCompareMode.STRICT));
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(1)).call(promptCaptor.capture());

        Prompt actualPrompt = promptCaptor.getValue();
        String userMessage = actualPrompt.getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        assertThat(userMessage).startsWith("Create a shop dialogue");
    }

    @Test
    void dialogueChat_whenLlmExhaustsAllRetries() throws Exception {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{}")))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Sorry, we could not fulfill your request please try again later"));
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        String firstUserMessage = getFirstUserMessage(allPrompts.get(0));
        assertThat(firstUserMessage).startsWith("Create a shop dialogue");


        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).startsWith("Create a shop dialogue");
            assertThat(retryMessage).containsOnlyOnce("info must not be null");
            assertThat(retryMessage).containsOnlyOnce("dialoguePhrases must not be empty");
            assertThat(retryMessage).containsOnlyOnce("peakers must not be empty");
        }
    }

    @Test
    void dialogueChat_withInvalidFields() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/invalid-fields.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Sorry, we could not fulfill your request please try again later"));

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("info.title must not be blank");
            assertThat(retryMessage).contains("info.transcription must not be blank");
            assertThat(retryMessage).contains("info.translations[0].translationText must not be blank");
            assertThat(retryMessage).contains("info.translations[0].isoLanguageCode Invalid ISO 639-1 language code");
            assertThat(retryMessage).contains("speakers[0].id must not be blank");
            assertThat(retryMessage).contains("speakers[0].title must not be blank");
            assertThat(retryMessage).contains("speakers[0].transcription must not be blank");
            assertThat(retryMessage).contains("speakers[0].translations[0].translationText must not be blank");
            assertThat(retryMessage).contains("speakers[0].translations[0].isoLanguageCode Invalid ISO 639-1 language code");
            assertThat(retryMessage).contains("dialoguePhrases[0].speakerId must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.phrase must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.isoLanguageCode Invalid ISO 639-1 language code");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.transcription must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.translations[0].translationText must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.translations[0].isoLanguageCode Invalid ISO 639-1 language code");
        }
    }

    @Test
    void dialogueChat_withNullRootFieldsFields() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/missing-fields.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("info must not be null");
            assertThat(retryMessage).contains("speakers must not be empty");
            assertThat(retryMessage).contains("dialoguePhrases must not be empty");
        }
    }

    @Test
    void dialogueChat_withEmptySpeakersAndEmptyDialoguePhrases() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/empty-speakers-and-dialogue-phrases.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("info must not be null");
            assertThat(retryMessage).contains("speakers must not be empty");
            assertThat(retryMessage).contains("dialoguePhrases must not be empty");
        }
    }

    @Test
    void dialogueChat_withEmptyTranslations() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/empty-translations.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("info.translations must not be empty");
            assertThat(retryMessage).contains("speakers[0].translations must not be empty");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.translations must not be empty");
        }
    }

    @Test
    void dialogueChat_withNullFieldsAndNonEmptyLists() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/null-fields-not-empty-lists.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("info.title must not be blank");
            assertThat(retryMessage).contains("info.transcription must not be blank");
            assertThat(retryMessage).contains("info.translations[0].translationText must not be blank");
            assertThat(retryMessage).contains("info.translations[0].isoLanguageCode must not be blank");
            assertThat(retryMessage).contains("speakers[0].id must not be blank");
            assertThat(retryMessage).contains("speakers[0].title must not be blank");
            assertThat(retryMessage).contains("speakers[0].transcription must not be blank");
            assertThat(retryMessage).contains("speakers[0].translations[0].translationText must not be blank");
            assertThat(retryMessage).contains("speakers[0].translations[0].isoLanguageCode must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].speakerId must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.phrase must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.isoLanguageCode must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.transcription must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.translations[0].translationText must not be blank");
            assertThat(retryMessage).contains("dialoguePhrases[0].phrase.translations[0].isoLanguageCode must not be blank");
        }
    }

    @Test
    void dialogueChat_whenPhraseReferencesUndefinedSpeaker() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/phrase-referencing-undefined-speaker.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("Phrase references undefined speaker: unknown_speaker");
        }
    }

    @Test
    void dialogueChat_whenDefinedSpeakerIsUnused() throws Exception {
        String llmResponseJson = readResourceToString("/structured-dialogue/unused-defined-speaker.json");

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponseJson)))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        for (int i = 1; i < allPrompts.size(); i++) {
            Prompt prompt = allPrompts.get(i);
            String retryMessage = getFirstUserMessage(prompt);
            assertThat(retryMessage).contains("Defined speaker is never used: speaker2");
        }
    }

    private static String getFirstUserMessage(Prompt prompt) {
        return prompt.getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    @Test
    void dialogueChat_whenLlmReturnsNullLiteral() throws Exception {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("null")))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Sorry, we could not fulfill your request please try again later"));
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        assertThat(allPrompts).first().satisfies(prompt ->
                assertThat(getFirstUserMessage(prompt))
                        .containsOnlyOnce("Create a shop dialogue")
                        .doesNotContain("Output was null. You must return a valid JSON object matching the requested schema."));

        String expectedMessageFragmentForFailedValidation =
                        """
                        Create a shop dialogue
                        Output validation failed because of: Output evaluated to null. You must return a valid JSON object matching the requested schema. Please correct these fields and regenerate.
                        """;
        assertThat(allPrompts).elements(1, 2, 3, 4, 5)
                .allSatisfy(prompt -> assertThat(getFirstUserMessage(prompt)).containsOnlyOnce(expectedMessageFragmentForFailedValidation));
    }

    @Test
    void dialogueChat_whenLlmOutputValidationFailsThenSucceeds() throws Exception {
        StructuredDialogueResource serviceResponse = StructuredDialogueResourceMother.DefaultStructuredDialogueResource.build();
        String jsonResponse = objectMapper.writeValueAsString(serviceResponse);

        when(chatModel.call(any(Prompt.class)))
                .thenReturn(
                        new ChatResponse(List.of(new Generation(new AssistantMessage("{}")))),
                        new ChatResponse(List.of(new Generation(new AssistantMessage("null")))),
                        new ChatResponse(List.of(new Generation(new AssistantMessage(jsonResponse)))));

        String requestJson = readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json");

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("responses/dialogue-chat-success-response.json"), JsonCompareMode.STRICT));
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(3)).call(promptCaptor.capture());
    }

    @Test
    void dialogueChat_whenLlmReturnsMalformedJson() throws Exception {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{ malformed json ]")))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Sorry, we could not fulfill your request please try again later"));

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(6)).call(promptCaptor.capture());
        List<Prompt> allPrompts = promptCaptor.getAllValues();

        assertThat(allPrompts).first().satisfies(prompt ->
                assertThat(getFirstUserMessage(prompt))
                        .containsOnlyOnce("Create a shop dialogue")
                        .doesNotContain("Output validation failed because of: Invalid JSON syntax"));

        assertThat(allPrompts).elements(1, 2, 3, 4, 5)
                .allSatisfy(prompt -> assertThat(getFirstUserMessage(prompt))
                        .contains("Output validation failed because of: Invalid JSON syntax")
                        .contains("Unexpected character ('m' (code 109)): was expecting double-quote to start property name")
                        .contains("You must return ONLY raw, valid JSON matching the requested schema without commentary."));
    }

    @Test
    void dialogueChat_validationFailure() throws Exception {
        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-invalid-request.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").isNotEmpty())
                .andExpect(jsonPath("$.errors.chatId").isNotEmpty());
    }
}
