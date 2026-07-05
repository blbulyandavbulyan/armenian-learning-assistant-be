package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.core.DialogueChatParameters;
import com.blbulyandavbulyan.larm.core.UserAwareDialogueChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class ChatController implements ChatApi {
    private final UserAwareDialogueChatService dialogueChatService;
    private final ChatMapper chatMapper;

    @Override
    public DialogueChatResponse dialogueChat(Jwt jwt, ChatRequest request) {
        StructuredDialogueResource structuredDialogueResource = dialogueChatService.dialogueChat(DialogueChatParameters.builder()
                        .chatId(request.chatId())
                        .message(request.message())
                        .issuer(jwt.getIssuer().toString())
                        .subject(jwt.getSubject())
                .build());
        return chatMapper.mapToDialogueResponse(structuredDialogueResource);
    }

}
