package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;

public record PhraseRecord(
        UUID id,
        String isoLanguageCode,
        String phrase,
        String transcription,
        Set<TranslationRecord> translations,
        Set<MediaRecord> mediaSet) {

    public PhraseRecord(Phrase phrase, Set<TranslationRecord> translations) {
        this(phrase.getId(), phrase.getIsoLanguageCode(), phrase.getPhrase(),
                phrase.getTranscription(), translations, getMediaRecords(phrase));
    }

    private static Set<MediaRecord> getMediaRecords(Phrase phrase) {
        return phrase.getMediaSet().stream().map(MediaRecord::new).collect(Collectors.toSet());
    }

}
