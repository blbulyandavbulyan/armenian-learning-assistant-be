package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record SaveDialogueRequest(
        @NotNull @Valid DialogueTitleRequest info,
        @NotEmpty List<@NotNull @Valid SpeakerRequest> speakers,
        @NotEmpty List<@NotNull @Valid DialoguePhraseRequest> dialoguePhrases) {

    record DialogueTitleRequest(
            @NotBlank String title,
            @NotBlank String transcription,
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    record SpeakerRequest(
            @NotBlank String id,
            @NotBlank String title,
            @NotBlank String transcription,
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    record DialoguePhraseRequest(
            @NotBlank String speakerId,
            @NotNull @Valid PhraseRequest phrase) {
    }

    record PhraseRequest(
            @NotBlank String phrase,
            @NotBlank String isoLanguageCode,
            @NotBlank String transcription,
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    record TranslationRequest(
            @NotBlank String translationText,
            @NotBlank String isoLanguageCode) {
    }
}
