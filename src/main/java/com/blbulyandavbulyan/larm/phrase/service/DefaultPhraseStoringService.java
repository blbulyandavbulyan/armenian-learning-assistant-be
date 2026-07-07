package com.blbulyandavbulyan.larm.phrase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.dao.repository.PhraseRepository;
import com.blbulyandavbulyan.larm.logging.Loggable;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
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

    @Transactional
    @Override
    @Loggable(logLevel = Loggable.LogLevel.DEBUG)
    public List<Phrase> batchSavePhrases(BatchSavePhrasesParameters parameters) {
        log.trace("Finding existing phrases by: {}", parameters.phrases());
        List<Phrase> existingEntities = phraseRepository.findByPhraseIn(parameters.phrases());
        log.trace("Existing phrases: {}", existingEntities);
        Map<String, Phrase> existingPhrasesMap = existingEntities.stream()
                .collect(Collectors.toMap(Phrase::getPhrase, Function.identity()));

        List<Phrase> assembledPhrases = new ArrayList<>();

        for (SavePhraseParameters phraseParams : parameters.phraseResources()) {
            Phrase existing = existingPhrasesMap.get(phraseParams.phrase());
            if (existing != null) {
                assembledPhrases.add(existing);
            } else {
                Phrase newPhrase = phraseMapper.mapToPhrase(phraseParams);
                existingPhrasesMap.put(phraseParams.phrase(), newPhrase);
                assembledPhrases.add(newPhrase);
            }
        }
        log.trace("Saving phrases {}", assembledPhrases);

        Iterable<Phrase> savedPhrases = phraseRepository.saveAll(assembledPhrases);

        return StreamSupport.stream(savedPhrases.spliterator(), false)
                .toList();
    }

}
