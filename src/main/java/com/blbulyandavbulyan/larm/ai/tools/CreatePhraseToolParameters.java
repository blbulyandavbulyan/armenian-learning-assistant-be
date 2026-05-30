package com.blbulyandavbulyan.larm.ai.tools;

import lombok.Builder;

import java.util.List;

@Builder
public record CreatePhraseToolParameters(
        String phrase,
        String transcription,
        List<CreateTranslationToolParameters> translations) {

}
