package com.blbulyandavbulyan.larm.phrase.service;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.IPhraseService;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseRepository;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesResult;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhraseService implements IPhraseService {
    private final PhraseRepository phraseRepository;
    private final PhraseMapper phraseMapper;
    private final IsoLanguageValidator isoLanguageValidator;

    @Transactional
    @Override
    public BatchSavePhrasesResult batchSavePhrases(BatchSavePhrasesParameters parameters) {
        validate(parameters);


        Set<String> alreadySavedPhrases = phraseRepository.findExistingPhrases(parameters.phrases());

        List<SavePhraseParameters> newPhrases = parameters.satisfyingPredicate(resource -> !alreadySavedPhrases.contains(resource.phrase()));

        Iterable<Phrase> savedPhrases = phraseRepository.saveAll(newPhrases.parallelStream().map(phraseMapper::mapToPhrase).toList());

        List<PhraseResource> mappedSavedPhrases = StreamSupport.stream(savedPhrases.spliterator(), true)
                .map(phraseMapper::mapFromPhrase)
                .toList();

        return BatchSavePhrasesResult.builder()
                .existingPhrases(alreadySavedPhrases)
                .savedPhrases(mappedSavedPhrases)
                .build();
    }

    private void validate(BatchSavePhrasesParameters batchParameters) {
        final List<CreateTranslationParameters> invalidTranslationParameters = new ArrayList<>();

        for (SavePhraseParameters phraseResource : batchParameters.phraseResources()) {
            for (CreateTranslationParameters translation : phraseResource.translations()) {
                if(!isoLanguageValidator.isValid(translation.isoLanguageCode())){
                    invalidTranslationParameters.add(translation);
                }
            }
        }

        if (!invalidTranslationParameters.isEmpty()) {
            throw new InvalidIsoLanguageCodeException(invalidTranslationParameters);
        }
    }


}
