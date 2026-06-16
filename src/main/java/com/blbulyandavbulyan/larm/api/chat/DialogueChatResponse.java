package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Dialogue Chat Response")
public record DialogueChatResponse(
        @Schema(description = Descriptions.CHAT_RESPONSE_MESSAGE, example = Examples.CHAT_RESPONSE_MESSAGE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DialogueTitleResponse info,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<SpeakerResponse> speakers,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<DialoguePhraseResponse> dialoguePhrases) {
}
