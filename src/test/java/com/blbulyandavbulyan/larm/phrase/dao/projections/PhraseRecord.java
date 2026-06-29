package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.dao.entities.PhraseStatus;

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

}
