package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.dao.entities.PhraseStatus;
import org.jspecify.annotations.NonNull;

public record PhraseRecord(
        UUID id,
        PhraseStatus status,
        String isoLanguageCode,
        String phrase,
        String transcription,
        Set<TranslationRecord> translations,
        Set<MediaRecord> mediaSet) {
    public PhraseRecord(Phrase phrase) {
        this(phrase.getId(), phrase.getStatus(), phrase.getIsoLanguageCode(), phrase.getPhrase(),
                phrase.getTranscription(), getTranslationRecords(phrase), getMediaRecords(phrase));
    }

    private static Set<MediaRecord> getMediaRecords(Phrase phrase) {
        return phrase.getMediaSet().stream().map(MediaRecord::new).collect(Collectors.toSet());
    }

    private static Set<TranslationRecord> getTranslationRecords(Phrase phrase) {
        return phrase.getTranslations().stream().map(TranslationRecord::new).collect(Collectors.toSet());
    }

    @SuppressWarnings({"DeconstructionCanBeUsed", "java:S6878"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhraseRecord that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(phrase, that.phrase)
                && status == that.status
                && Objects.equals(transcription, that.transcription)
                && Objects.equals(isoLanguageCode, that.isoLanguageCode)
                && Objects.equals(mediaSet, that.mediaSet)
                && Objects.equals(translations, that.translations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, isoLanguageCode, phrase, transcription,
                translations, mediaSet);
    }

    @Override
    @NonNull
    public String toString() {
        return "PhraseRecord{id=%s, status=%s, isoLanguageCode='%s', phrase='%s', transcription='%s', translations=%s, mediaSet=%s}"
                .formatted(id, status, isoLanguageCode, phrase, transcription, translations, mediaSet);
    }
}
