package com.blbulyandavbulyan.larm.ai;

import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;

public interface PhrasesChatService {
    StructuredPhrasesResource chat(String message, UUID chatId);
}
