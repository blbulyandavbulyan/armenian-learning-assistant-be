package com.blbulyandavbulyan.larm.phrase.service;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
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
        Phrase phrase = Phrase.builder()
                .id(resource.id())
                .phrase(resource.phrase())
                .transcription(resource.transcription())
                .isoLanguageCode(resource.isoLanguageCode())
                .status(PhraseStatus.DRAFT)
                .build();

        var translations = resource.translations().stream()
                .map(this::mapToTranslation)
                .peek(t -> t.setPhrase(phrase))
                .collect(Collectors.toSet());
        phrase.setTranslations(translations);

        var mediaSet = resource.mediaResources().stream()
                .map(mediaMapper::toMedia)
                .peek(m -> m.setPhrase(phrase))
                .collect(Collectors.toSet());
        phrase.setMediaSet(mediaSet);

        return phrase;
    }

    private Translation mapToTranslation(CreateTranslationParameters createTranslationParameters) {
        return Translation.builder()
                .id(UUID.randomUUID())
                .isoLanguageCode(createTranslationParameters.isoLanguageCode())
                .translationText(createTranslationParameters.translationText())
                .createdAt(Instant.now())
                .build();
    }
}
