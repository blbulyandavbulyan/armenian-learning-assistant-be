package com.blbulyandavbulyan.larm.core;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseOrchestrator {
    // TODO most probably we should move this guy somewhere, maybe in the phrase package

    private final PhraseStoringService phraseStoringService;
    private final PhraseProcessor phraseProcessor;

    @Timed(value = "save.phrases.total-time", description = "Includes total time for saving phrases including additional stuff like text-to-speach")
    public List<Phrase> savePhrases(List<NewCreatePhraseParameters> newCreatePhraseParameters) {
        final var phrasesWithMedias = initialProcess(newCreatePhraseParameters);
        return phraseStoringService.batchSavePhrases(phrasesWithMedias);
    }

    private BatchSavePhrasesParameters initialProcess(List<NewCreatePhraseParameters> newCreatePhraseParameters) {
        return new BatchSavePhrasesParameters(newCreatePhraseParameters.stream().parallel().map(phraseProcessor::process).toList());
    }
}
