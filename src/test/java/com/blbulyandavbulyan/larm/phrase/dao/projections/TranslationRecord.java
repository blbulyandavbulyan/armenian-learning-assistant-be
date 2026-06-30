package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContextualTranslation;

public record TranslationRecord(
        UUID id,
        String isoLanguageCode,
        String translationText) {
        
    public TranslationRecord(ContextualTranslation t) {
        this(t.getId(), t.getIsoLanguageCode(), t.getTranslationText());
    }
}
