package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder(toBuilder = true)
public record SavePhraseParameters(
        UUID id,
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<CreateTranslationParameters> translations,
        List<CreateMediaResource> mediaResources) {

    @SuppressWarnings({"DeconstructionCanBeUsed", "java:S6878"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SavePhraseParameters that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(phrase, that.phrase)
                && Objects.equals(isoLanguageCode, that.isoLanguageCode)
                && Objects.equals(transcription, that.transcription)
                && Objects.equals(translations, that.translations)
                && Objects.equals(mediaResources, that.mediaResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, phrase, isoLanguageCode, transcription, translations, mediaResources);
    }

    @Override
    @NonNull
    public String toString() {
        return "SavePhraseParameters{id=%s, phrase='%s', isoLanguageCode='%s', transcription='%s', translations=%s, mediaResources=%s}"
                .formatted(id, phrase, isoLanguageCode, transcription, translations, mediaResources);
    }
}
