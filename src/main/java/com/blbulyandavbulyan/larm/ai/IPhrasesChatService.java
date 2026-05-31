package com.blbulyandavbulyan.larm.ai;

import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;

import java.util.UUID;

public interface IPhrasesChatService {
    StructuredPhrasesResource chat(String message, UUID chatId);
}
