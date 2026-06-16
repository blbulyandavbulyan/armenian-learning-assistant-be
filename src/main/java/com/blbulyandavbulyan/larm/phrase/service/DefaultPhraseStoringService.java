package com.blbulyandavbulyan.larm.phrase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.InvalidIsoLanguageCodeException;
import com.blbulyandavbulyan.larm.phrase.PageParameters;
import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.PhrasesAlreadyExistException;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseRepository;
import com.blbulyandavbulyan.larm.validation.IsoLanguageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<PhraseResource> batchSavePhrases(BatchSavePhrasesParameters parameters) {
        validate(parameters);

        checkAndThrowIfSomePhrasesAlreadySaved(parameters);

        List<SavePhraseParameters> newPhrases = parameters.phraseResources();

        Iterable<Phrase> savedPhrases = phraseRepository.saveAll(newPhrases.parallelStream().map(phraseMapper::mapToPhrase).toList());

        return StreamSupport.stream(savedPhrases.spliterator(), true)
                .map(phraseMapper::mapFromPhrase)
                .toList();
    }

    private void checkAndThrowIfSomePhrasesAlreadySaved(BatchSavePhrasesParameters parameters) {
        Set<String> alreadySavedPhrases = phraseRepository.findExistingPhrases(parameters.phrases());
        if (!alreadySavedPhrases.isEmpty()) {
            throw new PhrasesAlreadyExistException(alreadySavedPhrases);
        }
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

    @Override
    public List<PhraseResource> getPhrasesByIds(Iterable<java.util.UUID> ids) {
        return StreamSupport.stream(phraseRepository.findAllById(ids).spliterator(), false)
                .map(phraseMapper::mapFromPhrase)
                .toList();
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
