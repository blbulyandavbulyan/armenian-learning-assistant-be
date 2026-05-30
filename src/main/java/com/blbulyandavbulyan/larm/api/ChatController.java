package com.blbulyandavbulyan.larm.api;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    // TODO if works extract into dedicated service
    private final ChatClient chatClient;

    public record ChatRequest(String message, UUID conversationId) {
    }

    public record ChatResponse(String message) {
    }

    @PostMapping
    public ChatResponse chat(ChatRequest request) {
        String content = chatClient.prompt()
                .user(request.message()).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.conversationId().toString()))
                .call()
                .content();
        return new ChatResponse(content);
    }
}
