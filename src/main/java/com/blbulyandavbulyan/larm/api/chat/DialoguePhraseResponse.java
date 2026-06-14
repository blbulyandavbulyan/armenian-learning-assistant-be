package com.blbulyandavbulyan.larm.api.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Dialogue Phrase Response")
public record DialoguePhraseResponse(
        SpeakerResponse speaker,
        DraftPhraseResponse phrase) {
}
