package com.blbulyandavbulyan.larm.phrase.service;

import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;

import java.util.List;
import java.util.function.Predicate;

public record BatchSavePhrasesParameters(List<SavePhraseParameters> phraseResources) {
    List<String> phrases() {
        return phraseResources.stream().map(SavePhraseParameters::phrase).toList();
    }

    List<SavePhraseParameters> satisfyingPredicate(Predicate<SavePhraseParameters> predicate) {
        return phraseResources.stream().filter(predicate).toList();
    }
}
