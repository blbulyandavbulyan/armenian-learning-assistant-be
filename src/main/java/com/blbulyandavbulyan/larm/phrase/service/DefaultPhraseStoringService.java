package com.blbulyandavbulyan.larm.phrase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.dao.repository.PhraseRepository;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.InvalidIsoLanguageCodeException;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.PhrasesAlreadyExistException;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.validation.IsoLanguageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultPhraseStoringService implements PhraseStoringService {
    private final PhraseRepository phraseRepository;
    private final PhraseMapper phraseMapper;
    private final IsoLanguageValidator isoLanguageValidator;

    @Transactional
    @Override
    public List<Phrase> batchSavePhrases(BatchSavePhrasesParameters parameters) {
        validate(parameters);

        checkAndThrowIfSomePhrasesAlreadySaved(parameters);

        List<SavePhraseParameters> newPhrases = parameters.phraseResources();

        Iterable<Phrase> savedPhrases = phraseRepository.saveAll(newPhrases.parallelStream().map(phraseMapper::mapToPhrase).toList());

        return StreamSupport.stream(savedPhrases.spliterator(), true)
                .toList();
    }

    private void checkAndThrowIfSomePhrasesAlreadySaved(BatchSavePhrasesParameters parameters) {
        Set<String> alreadySavedPhrases = phraseRepository.findExistingPhrases(parameters.phrases());
        if (!alreadySavedPhrases.isEmpty()) {
            throw new PhrasesAlreadyExistException(alreadySavedPhrases);
        }
    }

    private void validate(BatchSavePhrasesParameters batchParameters) {
        final List<CreateTranslationParameters> invalidTranslationParameters = new ArrayList<>();
        final List<SavePhraseParameters> invalidPhrases = new ArrayList<>();

        for (SavePhraseParameters phraseResource : batchParameters.phraseResources()) {
            if (isoLanguageValidator.isNotValid(phraseResource.isoLanguageCode())) {
                invalidPhrases.add(phraseResource);
            }
            for (CreateTranslationParameters translation : phraseResource.translations()) {
                if (isoLanguageValidator.isNotValid(translation.isoLanguageCode())) {
                    invalidTranslationParameters.add(translation);
                }
            }
        }

        if (!invalidTranslationParameters.isEmpty() || !invalidPhrases.isEmpty()) {
            throw new InvalidIsoLanguageCodeException(invalidTranslationParameters, invalidPhrases);
        }
    }

}
