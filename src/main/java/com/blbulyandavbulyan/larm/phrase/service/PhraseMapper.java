package com.blbulyandavbulyan.larm.phrase.service;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.TranslationResource;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseStatus;
import com.blbulyandavbulyan.larm.phrase.dao.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhraseMapper {
    private final MediaMapper mediaMapper;

    public Phrase mapToPhrase(SavePhraseParameters resource) {
        return Phrase.builder()
                .id(resource.id())
                .phrase(resource.phrase())
                .transcription(resource.transcription())
                .isoLanguageCode(resource.isoLanguageCode())
                .translations(resource.translations().stream()
                        .map(this::mapToTranslation)
                        .collect(Collectors.toSet()))
                .status(PhraseStatus.DRAFT)
                .isNewFlag(true)
                .mediaSet(resource.mediaResources().stream()
                        .map(mediaMapper::toMedia)
                        .collect(Collectors.toSet()))
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
                .isoLanguageCode(phrase.isoLanguageCode())
                .transcription(phrase.transcription())
                .translations(phrase.translations().stream().map(PhraseMapper::translationToResource).toList())
                .media(phrase.mediaSet().stream().map(mediaMapper::fromMedia).toList())
                .build();
    }

    private static TranslationResource translationToResource(Translation translation) {
        return TranslationResource.builder()
                .id(translation.id())
                .translationText(translation.translationText())
                .isoLanguageCode(translation.isoLanguageCode())
                .build();
    }
}
