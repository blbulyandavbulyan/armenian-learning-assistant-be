package com.blbulyandavbulyan.larm.core;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import lombok.Builder;

@Builder
public record SaveDialogueParameters(
        String title,
        String transcription,
        List<CreateTranslationParameters> titleTranslations,
        List<SpeakerParameters> speakers,
        List<DialoguePhraseParameters> dialoguePhrases) {

    @Builder
    public record SpeakerParameters(
            String speakerRefId,
            String title,
            String transcription,
            List<CreateTranslationParameters> translations) {
    }

    @Builder
    public record DialoguePhraseParameters(
            String speakerRefId,
            String phrase,
            String isoLanguageCode,
            String transcription,
            List<CreateTranslationParameters> translations) {
    }
}
