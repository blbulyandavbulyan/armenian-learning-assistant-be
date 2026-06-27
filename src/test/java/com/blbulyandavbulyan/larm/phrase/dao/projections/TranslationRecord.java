package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Translation;

public record TranslationRecord(
        UUID id,
        String isoLanguageCode,
        String translationText,
        Instant createdAt) {
    public TranslationRecord(Translation translation) {
        this(translation.getId(), translation.getIsoLanguageCode(), translation.getTranslationText(), translation.getCreatedAt());
    }
}
