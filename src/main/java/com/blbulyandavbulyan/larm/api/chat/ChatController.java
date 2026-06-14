package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.ai.PhrasesChatService;
import com.blbulyandavbulyan.larm.ai.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class ChatController implements ChatApi {
    private final PhrasesChatService phrasesChatService;
    private final ChatMapper chatMapper;

    @Override
    public @Valid PhraseChatResponse phrasesChat(ChatRequest request) {
        final StructuredPhrasesResource structuredPhrasesResource = phrasesChatService.phrasesChat(request.message(), request.chatId());
        return chatMapper.mapToChatResponse(structuredPhrasesResource);

    }

    @Override
    public DialogueChatResponse dialogueChat(ChatRequest request) {
        StructuredDialogueResource structuredDialogueResource = phrasesChatService.dialogueChat(request.message(), request.chatId());
        return chatMapper.mapToDialogueResponse(structuredDialogueResource);
    }

}
