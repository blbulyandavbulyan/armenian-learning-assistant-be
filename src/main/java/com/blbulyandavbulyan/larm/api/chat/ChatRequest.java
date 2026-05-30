package com.blbulyandavbulyan.larm.api.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatRequest(@NotBlank String message, @NotNull UUID chatId) {
}
