package com.blbulyandavbulyan.larm.phrase.service;

import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesResult;
import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.IPhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.PageParameters;
import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhraseStoringService implements IPhraseStoringService {
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

    @Override
    public PagedPhraseResource findAll(PageParameters pageParameters) {
        Pageable pageable = Pageable.ofSize(pageParameters.pageSize()).withPage(pageParameters.pageNumber() - 1);
        Page<Phrase> phrasePage = phraseRepository.findAll(pageable);
        return PagedPhraseResource.builder()
                .page(PagedPhraseResource.Page.builder().pageNumber(phrasePage.getNumber() + 1)
                        .totalPages(phrasePage.getTotalPages())
                        .pageSize(phrasePage.getSize())
                        .build())
                .phrases(phrasePage.stream().map(phraseMapper::mapFromPhrase).toList())
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
