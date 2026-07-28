package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.ai.StructuredDialogueResourceMother;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
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

    @BeforeEach
    void setUp() {
        when(chatModel.getDefaultOptions()).thenReturn(mock(ChatOptions.class, RETURNS_DEEP_STUBS));
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
    }

    @Test
    void dialogueChat_whenLlmExhaustsAllRetries_returns424() throws Exception {
        // Simulate the LLM returning a JSON response that fails Jakarta validation constraints (e.g. empty JSON object).
        // The JakartaValidationAdvisor will retry and eventually throw ConstraintViolationException.
        // DialogueChatService catches it and wraps it into UnfixableValidationException → 500
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{}")))));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Sorry, we could not fulfill your request please try again later"));
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
