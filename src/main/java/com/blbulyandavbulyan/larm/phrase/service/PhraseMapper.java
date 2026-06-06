package com.blbulyandavbulyan.larm.phrase.service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.TranslationResource;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseStatus;
import com.blbulyandavbulyan.larm.phrase.dao.Translation;
import org.springframework.stereotype.Component;

@Component
public class PhraseMapper {

    public Phrase mapToPhrase(SavePhraseParameters resource) {
        return Phrase.builder()
                .id(UUID.randomUUID())
                .phrase(resource.phrase())
                .transcription(resource.transcription())
                .isoLanguageCode(resource.isoLanguageCode())
                .translations(resource.translations().stream().map(this::mapToTranslation).collect(Collectors.toSet()))
                .status(PhraseStatus.DRAFT)
                .mediaSet(Collections.emptySet())
                .isNewFlag(true)
                .build();
    }

    private Translation mapToTranslation(CreateTranslationParameters createTranslationParameters) {
        return Translation.builder()
                .id(UUID.randomUUID())
                .isoLanguageCode(createTranslationParameters.isoLanguageCode())
                .translationText(createTranslationParameters.translationText())
                .createdAt(Instant.now())
                .build();
    }

    public PhraseResource mapFromPhrase(Phrase phrase) {
        return PhraseResource.builder()
                .id(phrase.id())
                .phrase(phrase.phrase())
                .iso2LanguageCode(phrase.isoLanguageCode())
                .transcription(phrase.transcription())
                .translations(phrase.translations().stream().map(PhraseMapper::translationToResource).toList())
                .build();
    }

    private static TranslationResource translationToResource(Translation translation) {
        return TranslationResource.builder()
                .id(translation.id())
                .translationText(translation.translationText())
                .iso2LanguageCode(translation.isoLanguageCode())
                .build();
    }
}
