package com.blbulyandavbulyan.larm.core;

import java.util.UUID;

import lombok.Builder;

@Builder
public record DialogueChatParameters(
        UUID chatId,
        String message,
        String issuer,
        String subject) {
}
