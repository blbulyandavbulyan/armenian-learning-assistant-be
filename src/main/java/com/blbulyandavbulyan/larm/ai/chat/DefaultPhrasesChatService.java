package com.blbulyandavbulyan.larm.ai.chat;

import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.PhrasesChatService;
import com.blbulyandavbulyan.larm.ai.StructuredDialogueResource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
public class DefaultPhrasesChatService implements PhrasesChatService {
    private final ChatClient armenianPhrasesGeneratorChatClient;

    @Override
    public StructuredPhrasesResource phrasesChat(String message, UUID chatId) {
        return armenianPhrasesGeneratorChatClient.prompt()
                .system(new ClassPathResource("prompts/ARMENIAN-PHRASES-GENERATOR.md"))
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId.toString()))
                .call()
                .entity(StructuredPhrasesResource.class);
    }

    @Override
//    @Validated
    public StructuredDialogueResource dialogueChat(String message, UUID chatId) {
        return armenianPhrasesGeneratorChatClient.prompt()
                .system(new ClassPathResource("prompts/ARMENIAN-DIALOGUE-GENERATOR.md"))
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId.toString()))
                .call()
                .entity(StructuredDialogueResource.class);
    }

}
