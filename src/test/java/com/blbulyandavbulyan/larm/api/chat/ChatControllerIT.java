package com.blbulyandavbulyan.larm.api.chat;

import java.util.Set;
import java.util.function.Consumer;

import com.blbulyandavbulyan.larm.ai.StructuredDialogueResourceMother;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.api.BaseIT;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private ChatClient chatClient;

    @SuppressWarnings("unchecked")
    private ChatClient.ChatClientRequestSpec buildPromptSpecMock() {
        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(any(ClassPathResource.class))).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        // Stub both overloads of advisors() so the mock chain does not return null
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Advisor.class))).thenReturn(promptSpec);
        return promptSpec;
    }

    @SuppressWarnings("unchecked")
    @Test
    void dialogueChat() throws Exception {
        StructuredDialogueResource serviceResponse = StructuredDialogueResourceMother.DefaultStructuredDialogueResource.build();

        ChatClient.ChatClientRequestSpec promptSpec = buildPromptSpecMock();
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(StructuredDialogueResource.class)).thenReturn(serviceResponse);

        String requestJson = readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json");

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("responses/dialogue-chat-success-response.json"), JsonCompareMode.STRICT));
    }

    @SuppressWarnings("unchecked")
    @Test
    void dialogueChat_whenLlmExhaustsAllRetries_returns424() throws Exception {
        // Simulate the advisor chain exhausting all retries and throwing ConstraintViolationException.
        // DialogueChatService catches it and wraps it into UnfixableValidationException → 424.
        ChatClient.ChatClientRequestSpec promptSpec = buildPromptSpecMock();
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(StructuredDialogueResource.class))
                .thenThrow(new ConstraintViolationException("LLM output failed validation", Set.of()));

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json")))
                // 500: the service failed to fulfill the request; 5xx is correct, 500 is appropriate since this is not a simple proxy
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Sorry, we could not fullfill your request please try again later"));
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
