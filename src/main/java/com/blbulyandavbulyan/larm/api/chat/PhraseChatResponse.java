package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Phrase Chat Response")
@Builder
record PhraseChatResponse(
        @Schema(description = Descriptions.PHRASE_CHAT_RESPONSE_MESSAGE, example = Examples.PHRASE_CHAT_RESPONSE_MESSAGE, requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<DraftPhraseResponse> phrases) {
}
