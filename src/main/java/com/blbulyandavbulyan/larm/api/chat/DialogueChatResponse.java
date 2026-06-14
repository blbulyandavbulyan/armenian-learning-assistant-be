package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DialogueChatResponse(
        // TODO probably you have to rename PHRASE_CHAT_RESPONSE_MESSAGE
        @Schema(description = OpenApiConstants.Descriptions.PHRASE_CHAT_RESPONSE_MESSAGE, example = OpenApiConstants.Examples.PHRASE_CHAT_RESPONSE_MESSAGE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,
        List<DialoguePhraseResponse> dialoguePhrases
) {
}
