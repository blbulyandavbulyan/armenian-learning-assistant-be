package com.blbulyandavbulyan.larm.api.chat;

import java.util.function.Consumer;

import com.blbulyandavbulyan.larm.ai.StructuredDialogueResourceMother;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.api.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
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
    @Test
    void dialogueChat() throws Exception {
        StructuredDialogueResource serviceResponse = StructuredDialogueResourceMother.DefaultStructuredDialogueResource.build();

        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(any(ClassPathResource.class))).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(StructuredDialogueResource.class)).thenReturn(serviceResponse);

        String requestJson = readResourceToString("/requests/chat/dialogue/dialogue-chat-request.json");

        mockMvc.perform(post(RequestMapping.DIALOGUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("responses/dialogue-chat-success-response.json"), JsonCompareMode.STRICT));
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
