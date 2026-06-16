package com.blbulyandavbulyan.larm.ai;

import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface PhrasesChatService {
    @Valid
    StructuredPhrasesResource phrasesChat(String message, UUID chatId);

    @Valid
    StructuredDialogueResource dialogueChat(String message, UUID chatId);
}
