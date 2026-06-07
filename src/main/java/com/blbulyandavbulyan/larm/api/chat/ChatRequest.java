package com.blbulyandavbulyan.larm.api.chat;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Chat Request")
record ChatRequest(
        @Schema(description = OpenApiConstants.Descriptions.CHAT_REQUEST_MESSAGE, example = OpenApiConstants.Examples.CHAT_REQUEST_MESSAGE)
        @NotBlank String message,

        @Schema(description = OpenApiConstants.Descriptions.CHAT_ID, example = OpenApiConstants.Examples.CHAT_ID)
        @NotNull UUID chatId) {
}
