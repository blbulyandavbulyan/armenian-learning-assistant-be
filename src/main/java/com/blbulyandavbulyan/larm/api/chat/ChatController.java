package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.ai.chat.DialogueChatService;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class ChatController implements ChatApi {
    private final DialogueChatService dialogueChatService;
    private final ChatMapper chatMapper;

    @Override
    public DialogueChatResponse dialogueChat(ChatRequest request) {
        StructuredDialogueResource structuredDialogueResource = dialogueChatService.dialogueChat(request.message(), request.chatId());
        return chatMapper.mapToDialogueResponse(structuredDialogueResource);
    }

}
