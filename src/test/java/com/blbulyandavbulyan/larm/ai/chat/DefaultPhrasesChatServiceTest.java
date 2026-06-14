package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.blbulyandavbulyan.larm.ai.StructuredDialogueResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPhrasesChatServiceTest {

    @Mock
    private ChatClient chatClient;

    private DefaultPhrasesChatService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPhrasesChatService(chatClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void phrasesChat_callsChatClientCorrectly() {
        // Arrange
        UUID chatId = UUID.randomUUID();
        String message = "Groceries";

        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        StructuredPhrasesResource expectedResponse = new StructuredPhrasesResource("Success", List.of());

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(any(ClassPathResource.class))).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(StructuredPhrasesResource.class)).thenReturn(expectedResponse);

        // Act
        StructuredPhrasesResource response = service.phrasesChat(message, chatId);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(chatClient).prompt();
        verify(promptSpec).system(any(ClassPathResource.class));
        verify(promptSpec).user(message);
        verify(promptSpec).advisors(any(Consumer.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void dialogueChat_callsChatClientCorrectly() {
        // Arrange
        UUID chatId = UUID.randomUUID();
        String message = "Greeting";

        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        StructuredDialogueResource expectedResponse = mock(StructuredDialogueResource.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(any(ClassPathResource.class))).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(StructuredDialogueResource.class)).thenReturn(expectedResponse);

        // Act
        StructuredDialogueResource response = service.dialogueChat(message, chatId);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(chatClient).prompt();
        verify(promptSpec).system(any(ClassPathResource.class));
        verify(promptSpec).user(message);
        verify(promptSpec).advisors(any(Consumer.class));
    }
}
