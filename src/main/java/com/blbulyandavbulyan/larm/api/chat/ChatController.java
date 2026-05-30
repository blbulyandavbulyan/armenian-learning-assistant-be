package com.blbulyandavbulyan.larm.api.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
public class ChatController {
    // TODO if works extract into dedicated service
    private final ChatClient chatClient;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String content = chatClient.prompt()
                .user(request.message()).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.chatId().toString()))
                .call()
                .content();
        return new ChatResponse(content);
    }
}
