package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import lombok.Builder;

@Builder
public record SpeakersResponse(
        String title,
        String transcription,
        List<TranslationResponse> translations) {

}
