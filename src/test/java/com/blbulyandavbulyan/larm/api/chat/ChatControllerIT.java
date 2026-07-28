package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;
import java.util.Objects;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.ai.StructuredDialogueResourceMother;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
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
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(1)).call(promptCaptor.capture());
        
        Prompt actualPrompt = promptCaptor.getValue();
        String userMessage = actualPrompt.getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        assertThat(userMessage).isEqualTo("Create a shop dialogue");
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
        // TODO, verify that it was called here with the right message first time (which user provided)
        //  and then other times with the 'modified' message which contains the error (you should include the full error here)
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
