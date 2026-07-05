package com.blbulyandavbulyan.larm.core;

import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.chat.DialogueChatService;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAwareDialogueChatService {
    private final UserService userService;
    private final DialogueChatService dialogueChatService;

    public StructuredDialogueResource dialogueChat(DialogueChatParameters dialogueChatParameters) {
        UUID userId = userService.aquireUserId(dialogueChatParameters.issuer(), dialogueChatParameters.subject());
        String secureChatId = userId + ":" + dialogueChatParameters.chatId();
        return dialogueChatService.dialogueChat(dialogueChatParameters.message(), secureChatId);
    }
}
