package com.blbulyandavbulyan.larm.ai.chat;

import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class DialogueChatService {
    private final ChatClient chatClient;

    @Valid
    public StructuredDialogueResource dialogueChat(String message, UUID chatId) {
        // TODO most probably we need here the tool which checks the existing phrases in the database
        //  probably it should check 'exact match' and 'similar' phrases
        // .tools()

        return chatClient.prompt()
                .system(new ClassPathResource("prompts/ARMENIAN-DIALOGUE-GENERATOR.md"))
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId.toString()))
                .call()
                .entity(StructuredDialogueResource.class);
    }

}
