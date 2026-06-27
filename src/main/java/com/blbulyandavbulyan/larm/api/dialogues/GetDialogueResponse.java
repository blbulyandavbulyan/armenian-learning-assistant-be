package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.phrases.PhraseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Get Dialogue Response")
@Builder
public record GetDialogueResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        PhraseResponse title,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<DialogueSpeakerResponse> speakers,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<DialoguePhraseResponse> dialoguePhrases) {
    @Schema(name = "Get Dialogue Speaker Response")
    @Builder
    public record DialogueSpeakerResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String speakerRefId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            PhraseResponse name) {
    }

    @Schema(name = "Get Dialogue Phrase Response")
    @Builder
    public record DialoguePhraseResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String speakerRefId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            PhraseResponse phrase) {
    }
}
