package com.blbulyandavbulyan.larm.api.chat;

import lombok.Builder;

@Builder
public record DialoguePhraseResponse(
        SpeakersResponse speaker,
        DraftPhraseResponse phrase) {
}
