package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Phrase Chat Response")
@Builder
record PhraseChatResponse(
        @Schema(description = "Response message (description of the results) from AI model")
        String message,

        List<DraftPhraseResponse> phrases) {
}
