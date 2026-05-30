package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.function.Predicate;

public record BatchSavePhrasesParameters(List<SavePhraseParameters> phraseResources) {
    public List<String> phrases() {
        return phraseResources.stream().map(SavePhraseParameters::phrase).toList();
    }

    public List<SavePhraseParameters> satisfyingPredicate(Predicate<SavePhraseParameters> predicate) {
        return phraseResources.stream().filter(predicate).toList();
    }
}
