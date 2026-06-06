package com.blbulyandavbulyan.larm.ai.chat;

import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.PhrasesChatService;
import com.blbulyandavbulyan.larm.ai.chat.common.ScopedValues;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultPhrasesChatService implements PhrasesChatService {
    private final ChatClient armenianPhrasesGeneratorChatClient;

    @Override
    public StructuredPhrasesResource chat(String message, UUID chatId) {
        return ScopedValue.where(ScopedValues.CONVERSATION_ID, chatId)
                .call(() -> chatInternal(message, chatId));
    }

    private StructuredPhrasesResource chatInternal(String message, UUID chatId) {
        return armenianPhrasesGeneratorChatClient.prompt().user(message).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId.toString()))
                .call()
                .entity(StructuredPhrasesResource.class);
    }
}
